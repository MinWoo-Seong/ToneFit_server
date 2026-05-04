package com.example.ToneFit.correction.dto;

import com.example.ToneFit.session.model.Status;

import java.time.LocalDateTime;

public record EditResponse(
        Long sessionId,
        Status status,
        LocalDateTime updatedAt
) {
}
