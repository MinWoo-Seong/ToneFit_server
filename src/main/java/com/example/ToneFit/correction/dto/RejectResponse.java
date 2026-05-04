package com.example.ToneFit.correction.dto;

import com.example.ToneFit.correction.model.Action;

import java.time.LocalDateTime;

public record RejectResponse(
        Long sessionId,
        int index,
        Action action,
        LocalDateTime updatedAt
) {
}
