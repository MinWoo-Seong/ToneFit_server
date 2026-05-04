package com.example.ToneFit.correction.dto;

import com.example.ToneFit.session.model.Purpose;
import com.example.ToneFit.session.model.Receiver;
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
