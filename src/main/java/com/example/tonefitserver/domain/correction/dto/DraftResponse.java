package com.example.tonefitserver.domain.correction.dto;

import com.example.tonefitserver.domain.session.Purpose;
import com.example.tonefitserver.domain.session.Receiver;

import java.time.LocalDateTime;

public record DraftResponse(
        Long sessionId,
        Receiver receiverType,
        Purpose purpose,
        String subject,
        String originalEmail,
        LocalDateTime updatedAt
) {
}
