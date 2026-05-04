package com.example.ToneFit.correction.dto;

import com.example.ToneFit.session.model.Status;

import java.time.LocalDateTime;

public record FinalizeResponse(
        Long sessionId,
        Status status,
        String aiFinal,
        String aiSubject,
        LocalDateTime createdAt
) {
}
