package com.example.ToneFit.correction.controller;

import com.example.ToneFit.correction.dto.*;
import com.example.ToneFit.correction.service.CorrectionService;
import com.example.ToneFit.session.model.Purpose;
import com.example.ToneFit.session.model.Receiver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/corrections")
@RequiredArgsConstructor
public class CorrectionController {

    private final CorrectionService correctionService;

    @PutMapping("/draft")
    public ResponseEntity<DraftResponse> saveDraft(@AuthenticationPrincipal String email,
                                                   @RequestBody DraftSaveRequest request) {
        Optional<DraftResponse> result = correctionService.saveDraft(email, request);
        return result
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/draft")
    public DraftResponse getDraft(@AuthenticationPrincipal String email) {
        return correctionService.getDraft(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CorrectionResponse correct(@AuthenticationPrincipal String email,
                                      @Valid @RequestBody CorrectionRequest request) {
        return correctionService.correct(email, request);
    }

    @PostMapping("/{sessionId}/recorrect")
    public CorrectionResponse recorrect(@AuthenticationPrincipal String email,
                                        @PathVariable Long sessionId,
                                        @Valid @RequestBody RecorrectRequest request) {
        return correctionService.recorrect(email, sessionId, request);
    }

    @PostMapping("/{sessionId}/reject")
    public RejectResponse rejectFeedback(@AuthenticationPrincipal String email,
                                         @PathVariable Long sessionId,
                                         @Valid @RequestBody RejectRequest request) {
        return correctionService.rejectFeedback(email, sessionId, request);
    }

    @PostMapping("/{sessionId}/finalize")
    public FinalizeResponse finalizeSession(@AuthenticationPrincipal String email,
                                            @PathVariable Long sessionId) {
        return correctionService.finalize(email, sessionId);
    }

    @PatchMapping("/{sessionId}/edit")
    public EditResponse editFinal(@AuthenticationPrincipal String email,
                                  @PathVariable Long sessionId,
                                  @RequestBody EditRequest request) {
        return correctionService.editFinal(email, sessionId, request);
    }

    @PostMapping("/{sessionId}/confirm")
    public ConfirmResponse confirmFinal(@AuthenticationPrincipal String email,
                                        @PathVariable Long sessionId,
                                        @RequestBody(required = false) ConfirmRequest request) {
        return correctionService.confirmFinal(email, sessionId, request);
    }

    @GetMapping("/in-progress")
    public InProgressResponse listInProgress(@AuthenticationPrincipal String email) {
        return correctionService.listInProgress(email);
    }

    @GetMapping("/history")
    public HistoryResponse listHistory(@AuthenticationPrincipal String email,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(value = "receiver_type", required = false) Receiver receiverType,
                                       @RequestParam(required = false) Purpose purpose) {
        return correctionService.listHistory(email, page, size, receiverType, purpose);
    }

    @GetMapping("/{sessionId}")
    public CorrectionDetailResponse getDetail(@AuthenticationPrincipal String email,
                                              @PathVariable Long sessionId) {
        return correctionService.getDetail(email, sessionId);
    }
}
