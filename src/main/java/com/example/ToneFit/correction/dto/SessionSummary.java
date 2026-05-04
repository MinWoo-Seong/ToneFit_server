package com.example.ToneFit.correction.dto;

import com.example.ToneFit.session.model.Purpose;
import com.example.ToneFit.session.model.Receiver;
import com.example.ToneFit.session.model.Status;

import java.time.LocalDateTime;

public record SessionSummary(
        Long sessionId,
        Receiver receiverType,
        Purpose purpose,
        String subject,
        Status status,
        String originalPreview,
        LocalDateTime createdAt
) {
}
