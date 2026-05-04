package com.example.tonefitserver.domain.correction.service;

import com.example.tonefitserver.core.enums.ErrorType;
import com.example.tonefitserver.core.enums.UserStatus;
import com.example.tonefitserver.core.exception.BusinessException;
import com.example.tonefitserver.domain.correction.ai.AiCorrectionClient;
import com.example.tonefitserver.domain.correction.ai.AiCorrectionResult;
import com.example.tonefitserver.domain.correction.ai.AiFinalizeResult;
import com.example.tonefitserver.domain.correction.dto.ConfirmRequest;
import com.example.tonefitserver.domain.correction.dto.ConfirmResponse;
import com.example.tonefitserver.domain.correction.dto.CorrectionDetailResponse;
import com.example.tonefitserver.domain.correction.dto.CorrectionRequest;
import com.example.tonefitserver.domain.correction.dto.CorrectionResponse;
import com.example.tonefitserver.domain.correction.dto.DraftResponse;
import com.example.tonefitserver.domain.correction.dto.DraftSaveRequest;
import com.example.tonefitserver.domain.correction.dto.EditRequest;
import com.example.tonefitserver.domain.correction.dto.EditResponse;
import com.example.tonefitserver.domain.correction.dto.FinalizeResponse;
import com.example.tonefitserver.domain.correction.dto.HistoryResponse;
import com.example.tonefitserver.domain.correction.dto.InProgressResponse;
import com.example.tonefitserver.domain.correction.dto.RecorrectRequest;
import com.example.tonefitserver.domain.correction.dto.RejectRequest;
import com.example.tonefitserver.domain.correction.dto.RejectResponse;
import com.example.tonefitserver.domain.correction.dto.SessionSummary;
import com.example.tonefitserver.domain.correction.model.Action;
import com.example.tonefitserver.domain.correction.model.CorrectionFeedback;
import com.example.tonefitserver.domain.correction.repository.CorrectionFeedbackRepository;
import com.example.tonefitserver.domain.event.EventService;
import com.example.tonefitserver.domain.event.EventType;
import com.example.tonefitserver.domain.prompt.PromptPurpose;
import com.example.tonefitserver.domain.prompt.PromptVersion;
import com.example.tonefitserver.domain.prompt.PromptVersionRepository;
import com.example.tonefitserver.domain.session.CorrectionSession;
import com.example.tonefitserver.domain.session.CorrectionSessionRepository;
import com.example.tonefitserver.domain.session.Purpose;
import com.example.tonefitserver.domain.session.Range;
import com.example.tonefitserver.domain.session.Receiver;
import com.example.tonefitserver.domain.session.Status;
import com.example.tonefitserver.domain.user.User;
import com.example.tonefitserver.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CorrectionService {

    private static final int PREVIEW_LENGTH = 50;
    private static final List<Status> IN_PROGRESS_STATUSES = List.of(Status.IN_PROGRESS, Status.EDITING);

    private final CorrectionSessionRepository sessionRepository;
    private final CorrectionFeedbackRepository feedbackRepository;
    private final PromptVersionRepository promptVersionRepository;
    private final UserRepository userRepository;
    private final AiCorrectionClient aiClient;
    private final EventService eventService;

    @Transactional
    public Optional<DraftResponse> saveDraft(Long userId, DraftSaveRequest req) {
        User user = loadUser(userId);
        boolean allNull = req.receiverType() == null
                && req.purpose() == null
                && (req.subject() == null || req.subject().isBlank())
                && (req.originalEmail() == null || req.originalEmail().isBlank());

        Optional<CorrectionSession> existing = sessionRepository.findByUserIdAndStatus(user.getId(), Status.DRAFT);

        if (allNull) {
            existing.ifPresent(sessionRepository::delete);
            return Optional.empty();
        }

        CorrectionSession session = existing.orElseGet(() -> CorrectionSession.builder()
                .user(user)
                .status(Status.DRAFT)
                .build());
        session.updateDraft(req.receiverType(), req.purpose(), req.subject(), req.originalEmail());
        CorrectionSession saved = sessionRepository.save(session);
        return Optional.of(toDraftResponse(saved));
    }

    @Transactional(readOnly = true)
    public DraftResponse getDraft(Long userId) {
        User user = loadUser(userId);
        CorrectionSession session = sessionRepository.findByUserIdAndStatus(user.getId(), Status.DRAFT)
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND, "임시 저장이 없습니다."));
        return toDraftResponse(session);
    }

    @Transactional
    public CorrectionResponse correct(Long userId, CorrectionRequest req) {
        User user = loadUser(userId);
        CorrectionSession session = (req.sessionId() != null)
                ? findOwnedSession(user.getId(), req.sessionId())
                : CorrectionSession.builder().user(user).status(Status.DRAFT).build();

        if (req.sessionId() != null && session.getStatus() != Status.DRAFT) {
            throw new BusinessException(ErrorType.INVALID_REQUEST,
                    "DRAFT 상태의 세션에서만 1차 교정을 시작할 수 있습니다. 진행 중 세션은 /recorrect를 사용하세요.");
        }

        session.updateDraft(req.receiverType(), req.purpose(), req.subject(), req.originalEmail());
        session.updateProtectedRanges(toRanges(req.protectedRanges()));
        session.updateInitialPromptVersion(activeInitialPrompt());
        session.updateStatus(Status.IN_PROGRESS);
        CorrectionSession saved = sessionRepository.save(session);

        feedbackRepository.deleteBySessionId(saved.getId());

        AiCorrectionResult result;
        try {
            result = aiClient.correct(
                    saved.getInitialPromptVersion() != null ? saved.getInitialPromptVersion().getContent() : null,
                    saved.getReceiverType(),
                    saved.getPurpose(),
                    saved.getOriginal(),
                    saved.getProtectedRanges()
            );
        } catch (Exception e) {
            saved.updateStatus(Status.DRAFT);
            throw new BusinessException(ErrorType.AI_SERVICE_ERROR,
                    ErrorType.AI_SERVICE_ERROR.getMessage(), saved.getId(), e);
        }

        List<CorrectionFeedback> feedbacks = result.changes().stream()
                .map(c -> CorrectionFeedback.builder()
                        .user(saved.getUser())
                        .session(saved)
                        .index(c.index())
                        .start(c.start())
                        .end(c.end())
                        .original(c.original())
                        .corrected(c.corrected())
                        .reason(c.reason())
                        .label(c.label())
                        .confidence(c.confidence())
                        .appliedRules(c.appliedRules())
                        .build())
                .toList();
        feedbackRepository.saveAll(feedbacks);

        eventService.record(user, EventType.STARTED, saved,
                Map.of("input_length", saved.getOriginal() == null ? 0 : saved.getOriginal().length()));

        return toCorrectionResponse(saved, result);
    }

    @Transactional
    public CorrectionResponse recorrect(Long userId, Long sessionId, RecorrectRequest req) {
        User user = loadUser(userId);
        CorrectionSession session = findOwnedSession(user.getId(), sessionId);
        if (session.getStatus() != Status.IN_PROGRESS) {
            throw new BusinessException(ErrorType.INVALID_REQUEST,
                    "IN_PROGRESS 상태에서만 재교정할 수 있습니다.");
        }
        session.updateReceiverPurpose(req.receiverType(), req.purpose());
        session.updateInitialPromptVersion(activeInitialPrompt());
        session.updateStatus(Status.IN_PROGRESS);

        feedbackRepository.deleteBySessionId(sessionId);

        AiCorrectionResult result;
        try {
            result = aiClient.correct(
                    session.getInitialPromptVersion() != null ? session.getInitialPromptVersion().getContent() : null,
                    session.getReceiverType(),
                    session.getPurpose(),
                    session.getOriginal(),
                    session.getProtectedRanges()
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorType.AI_SERVICE_ERROR,
                    ErrorType.AI_SERVICE_ERROR.getMessage(), sessionId, e);
        }

        List<CorrectionFeedback> feedbacks = result.changes().stream()
                .map(c -> CorrectionFeedback.builder()
                        .user(session.getUser())
                        .session(session)
                        .index(c.index())
                        .start(c.start())
                        .end(c.end())
                        .original(c.original())
                        .corrected(c.corrected())
                        .reason(c.reason())
                        .label(c.label())
                        .confidence(c.confidence())
                        .appliedRules(c.appliedRules())
                        .build())
                .toList();
        feedbackRepository.saveAll(feedbacks);

        return toCorrectionResponse(session, result);
    }

    @Transactional
    public RejectResponse rejectFeedback(Long userId, Long sessionId, RejectRequest req) {
        User user = loadUser(userId);
        CorrectionSession session = findOwnedSession(user.getId(), sessionId);
        if (session.getStatus() != Status.IN_PROGRESS) {
            throw new BusinessException(ErrorType.INVALID_REQUEST,
                    "IN_PROGRESS 상태에서만 교정을 거절할 수 있습니다.");
        }
        CorrectionFeedback feedback = feedbackRepository.findBySessionIdAndIndex(sessionId, req.index())
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND, "교정 건을 찾을 수 없습니다."));
        feedback.reject(req.reasonPrimary(), req.reasonSecondary(), req.reasonText());

        Map<String, Object> payload = new HashMap<>();
        payload.put("correction_index", feedback.getIndex());
        payload.put("reason_code", req.reasonPrimary() == null ? null : req.reasonPrimary().name());
        payload.put("recipient_type", session.getReceiverType() == null ? null : session.getReceiverType().name());
        eventService.record(user, EventType.REJECTED, session, payload);

        return new RejectResponse(session.getId(), feedback.getIndex(), Action.REJECTED, feedback.getUpdatedAt());
    }

    @Transactional
    public FinalizeResponse finalize(Long userId, Long sessionId) {
        User user = loadUser(userId);
        CorrectionSession session = findOwnedSession(user.getId(), sessionId);
        if (session.getStatus() != Status.IN_PROGRESS) {
            throw new BusinessException(ErrorType.INVALID_REQUEST,
                    "IN_PROGRESS 상태에서만 최종 다듬기를 진행할 수 있습니다.");
        }
        List<CorrectionFeedback> feedbacks = feedbackRepository.findBySessionIdOrderByIndexAsc(sessionId);

        feedbacks.stream().filter(f -> f.getAction() == null).forEach(CorrectionFeedback::accept);

        MergeResult merge = mergeForFinalize(session.getOriginal(), session.getProtectedRanges(), feedbacks);
        PromptVersion finalPrompt = activeFinalPrompt();
        session.updateFinalPromptVersion(finalPrompt);

        AiFinalizeResult result;
        try {
            result = aiClient.finalizePolish(
                    finalPrompt != null ? finalPrompt.getContent() : null,
                    session.getReceiverType(),
                    session.getPurpose(),
                    merge.mergedText(),
                    merge.protectedRanges()
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorType.AI_SERVICE_ERROR,
                    ErrorType.AI_SERVICE_ERROR.getMessage(), sessionId, e);
        }

        session.updateAiResult(result.aiFinal(), result.aiSubject());
        session.updateStatus(Status.EDITING);

        eventService.record(user, EventType.COMPLETED, session, null);

        return new FinalizeResponse(session.getId(), session.getStatus(),
                session.getAiFinal(), session.getAiSubject(), session.getCreatedAt());
    }

    @Transactional
    public EditResponse editFinal(Long userId, Long sessionId, EditRequest req) {
        User user = loadUser(userId);
        CorrectionSession session = findOwnedSession(user.getId(), sessionId);
        if (session.getStatus() != Status.EDITING) {
            throw new BusinessException(ErrorType.INVALID_REQUEST, "EDITING 상태에서만 편집할 수 있습니다.");
        }
        session.updateUserEdit(req.userFinal(), req.userSubject());
        return new EditResponse(session.getId(), session.getStatus(), session.getUpdatedAt());
    }

    @Transactional
    public ConfirmResponse confirmFinal(Long userId, Long sessionId, ConfirmRequest req) {
        User user = loadUser(userId);
        CorrectionSession session = findOwnedSession(user.getId(), sessionId);
        if (session.getStatus() != Status.EDITING) {
            throw new BusinessException(ErrorType.INVALID_REQUEST, "EDITING 상태에서만 확정할 수 있습니다.");
        }
        boolean edited = req != null
                && (req.userFinal() != null || req.userSubject() != null);
        if (req != null) {
            session.updateUserEdit(req.userFinal(), req.userSubject());
        }
        session.updateStatus(Status.CONFIRMED);

        eventService.record(user, EventType.COPIED, session, Map.of("edited", edited));

        return new ConfirmResponse(session.getId(), session.getStatus(), session.getUpdatedAt());
    }

    @Transactional(readOnly = true)
    public InProgressResponse listInProgress(Long userId) {
        User user = loadUser(userId);
        List<SessionSummary> summaries = sessionRepository
                .findByUserIdAndStatusIn(user.getId(), IN_PROGRESS_STATUSES).stream()
                .map(this::toSessionSummary)
                .toList();
        return new InProgressResponse(summaries);
    }

    @Transactional(readOnly = true)
    public HistoryResponse listHistory(Long userId, int page, int size,
                                       Receiver receiverType, Purpose purpose) {
        User user = loadUser(userId);
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        Page<CorrectionSession> result = sessionRepository
                .searchHistory(user.getId(), Status.CONFIRMED, receiverType, purpose, pageable);
        List<SessionSummary> summaries = result.getContent().stream()
                .map(this::toSessionSummary)
                .toList();
        return new HistoryResponse((int) result.getTotalElements(), page, size, summaries);
    }

    @Transactional(readOnly = true)
    public CorrectionDetailResponse getDetail(Long userId, Long sessionId) {
        User user = loadUser(userId);
        CorrectionSession session = findOwnedSession(user.getId(), sessionId);
        List<CorrectionDetailResponse.FeedbackItem> feedbacks = feedbackRepository
                .findBySessionIdOrderByIndexAsc(sessionId).stream()
                .map(f -> new CorrectionDetailResponse.FeedbackItem(
                        f.getIndex(), f.getStart(), f.getEnd(),
                        f.getOriginal(), f.getCorrected(), f.getReason(),
                        f.getLabel(), f.getConfidence(), f.getAppliedRules(),
                        f.getAction(), f.getReasonPrimary(), f.getReasonSecondary(), f.getReasonText()))
                .toList();

        return new CorrectionDetailResponse(
                session.getId(),
                session.getReceiverType(),
                session.getPurpose(),
                session.getSubject(),
                session.getOriginal(),
                session.getAiFinal(),
                session.getUserFinal(),
                session.getAiSubject(),
                session.getUserSubject(),
                session.getStatus(),
                feedbacks,
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

    private User loadUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorType.UNAUTHORIZED);
        }
        return userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorType.UNAUTHORIZED));
    }

    private CorrectionSession findOwnedSession(Long userId, Long sessionId) {
        CorrectionSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND, "세션을 찾을 수 없습니다."));
        if (!session.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorType.FORBIDDEN);
        }
        return session;
    }

    private PromptVersion activeInitialPrompt() {
        return promptVersionRepository
                .findFirstByPurposeAndIsActiveTrueOrderByCreatedAtDesc(PromptPurpose.INITIAL)
                .orElse(null);
    }

    private PromptVersion activeFinalPrompt() {
        return promptVersionRepository
                .findFirstByPurposeAndIsActiveTrueOrderByCreatedAtDesc(PromptPurpose.FINAL)
                .orElse(null);
    }

    private List<Range> toRanges(List<CorrectionRequest.ProtectedRange> ranges) {
        if (ranges == null) return List.of();
        return ranges.stream().map(r -> new Range(r.start(), r.end())).toList();
    }

    /**
     * finalize 단계 입력 텍스트와 거기서의 보호 구간을 함께 산출한다.
     *
     * <p>보호 구간은 다음 셋의 합집합이다:
     * <ul>
     *   <li>수락된(ACCEPTED) feedback의 corrected 텍스트가 들어간 위치</li>
     *   <li>거절된(REJECTED) feedback의 original 텍스트가 그대로 남아 있는 위치</li>
     *   <li>FE가 처음에 지정한 session.protectedRanges (merged 좌표로 변환)</li>
     * </ul>
     */
    private MergeResult mergeForFinalize(String original,
                                         List<Range> originalProtected,
                                         List<CorrectionFeedback> feedbacks) {
        if (original == null) return new MergeResult(null, List.of());

        List<CorrectionFeedback> sorted = feedbacks.stream()
                .sorted((a, b) -> Integer.compare(a.getStart(), b.getStart()))
                .toList();

        StringBuilder out = new StringBuilder();
        List<Range> protectedInMerged = new ArrayList<>();
        int origCursor = 0;
        for (CorrectionFeedback f : sorted) {
            if (f.getStart() < origCursor) continue;
            out.append(original, origCursor, f.getStart());
            int atomStart = out.length();
            String replacement = f.getAction() == Action.ACCEPTED ? f.getCorrected() : f.getOriginal();
            out.append(replacement);
            int atomEnd = out.length();
            if (atomEnd > atomStart) {
                protectedInMerged.add(new Range(atomStart, atomEnd));
            }
            origCursor = f.getEnd();
        }
        if (origCursor < original.length()) {
            out.append(original, origCursor, original.length());
        }

        // FE가 지정했던 원문 보호 구간을 merged 좌표로 변환해 추가
        if (originalProtected != null) {
            for (Range r : originalProtected) {
                int ms = translateOrigToMerged(r.getStart(), sorted);
                int me = translateOrigToMerged(r.getEnd(), sorted);
                if (ms < me) protectedInMerged.add(new Range(ms, me));
            }
        }

        return new MergeResult(out.toString(), coalesceRanges(protectedInMerged));
    }

    /** original 텍스트의 위치를 merged 텍스트 좌표로 변환한다. */
    private int translateOrigToMerged(int origPos, List<CorrectionFeedback> sortedFeedbacks) {
        int delta = 0;
        for (CorrectionFeedback f : sortedFeedbacks) {
            if (f.getEnd() <= origPos) {
                String replacement = f.getAction() == Action.ACCEPTED ? f.getCorrected() : f.getOriginal();
                delta += replacement.length() - (f.getEnd() - f.getStart());
            } else if (f.getStart() < origPos) {
                return f.getStart() + delta;
            } else {
                break;
            }
        }
        return origPos + delta;
    }

    private List<Range> coalesceRanges(List<Range> ranges) {
        if (ranges.isEmpty()) return List.of();
        List<Range> sorted = new ArrayList<>(ranges);
        sorted.sort(Comparator.comparingInt(Range::getStart));
        List<Range> result = new ArrayList<>();
        Range cur = sorted.get(0);
        for (int i = 1; i < sorted.size(); i++) {
            Range nxt = sorted.get(i);
            if (nxt.getStart() <= cur.getEnd()) {
                cur = new Range(cur.getStart(), Math.max(cur.getEnd(), nxt.getEnd()));
            } else {
                result.add(cur);
                cur = nxt;
            }
        }
        result.add(cur);
        return result;
    }

    private record MergeResult(String mergedText, List<Range> protectedRanges) {
    }

    private DraftResponse toDraftResponse(CorrectionSession s) {
        return new DraftResponse(s.getId(), s.getReceiverType(), s.getPurpose(),
                s.getSubject(), s.getOriginal(), s.getUpdatedAt());
    }

    private CorrectionResponse toCorrectionResponse(CorrectionSession s, AiCorrectionResult r) {
        List<CorrectionResponse.ChangeItem> items = r.changes().stream()
                .map(c -> new CorrectionResponse.ChangeItem(
                        c.index(), c.start(), c.end(), c.original(), c.corrected(), c.reason(),
                        c.label(), c.confidence(), c.appliedRules(), null))
                .toList();
        return new CorrectionResponse(s.getId(), r.correctedEmail(), items, s.getUpdatedAt());
    }

    private SessionSummary toSessionSummary(CorrectionSession s) {
        String preview = s.getOriginal() == null ? null
                : s.getOriginal().substring(0, Math.min(PREVIEW_LENGTH, s.getOriginal().length()));
        return new SessionSummary(s.getId(), s.getReceiverType(), s.getPurpose(),
                s.getSubject(), s.getStatus(), preview, s.getCreatedAt());
    }
}
