package com.example.ToneFit.correction.dto;

import com.example.ToneFit.session.model.Purpose;
import com.example.ToneFit.session.model.Receiver;

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
