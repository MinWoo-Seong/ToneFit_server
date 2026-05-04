package com.example.ToneFit.correction.dto;

import com.example.ToneFit.correction.model.Action;
import com.example.ToneFit.correction.model.Label;
import com.example.ToneFit.correction.model.ReasonPrimary;
import com.example.ToneFit.correction.model.ReasonSecondary;
import com.example.ToneFit.session.model.Purpose;
import com.example.ToneFit.session.model.Receiver;
import com.example.ToneFit.session.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public record CorrectionDetailResponse(
        Long sessionId,
        Receiver receiverType,
        Purpose purpose,
        String subject,
        String originalEmail,
        String aiFinal,
        String userFinal,
        String aiSubject,
        String userSubject,
        Status status,
        List<FeedbackItem> feedbacks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record FeedbackItem(
            int index,
            int start,
            int end,
            String original,
            String corrected,
            String reason,
            Label label,
            double confidence,
            List<String> appliedRules,
            Action action,
            ReasonPrimary reasonPrimary,
            ReasonSecondary reasonSecondary,
            String reasonText
    ) {
    }
}
