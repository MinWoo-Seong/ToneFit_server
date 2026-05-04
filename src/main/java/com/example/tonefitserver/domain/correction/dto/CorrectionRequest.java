package com.example.tonefitserver.domain.correction.dto;

import com.example.tonefitserver.domain.session.Purpose;
import com.example.tonefitserver.domain.session.Receiver;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CorrectionRequest(
        Long sessionId,
        @NotNull Receiver receiverType,
        @NotNull Purpose purpose,
        String subject,
        @NotBlank String originalEmail,
        List<ProtectedRange> protectedRanges
) {
    public record ProtectedRange(int start, int end) {
    }
}
