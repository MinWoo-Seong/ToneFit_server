package com.example.ToneFit.correction.dto;

import com.example.ToneFit.correction.model.ReasonPrimary;
import com.example.ToneFit.correction.model.ReasonSecondary;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RejectRequest(
        @NotNull Integer index,
        ReasonPrimary reasonPrimary,
        ReasonSecondary reasonSecondary,
        @Size(max = 200) String reasonText
) {
}
