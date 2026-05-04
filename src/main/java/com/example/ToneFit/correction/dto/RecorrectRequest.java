package com.example.ToneFit.correction.dto;

import com.example.ToneFit.session.model.Purpose;
import com.example.ToneFit.session.model.Receiver;
import jakarta.validation.constraints.AssertTrue;

public record RecorrectRequest(
        Receiver receiverType,
        Purpose purpose
) {
    @AssertTrue(message = "receiverType 또는 purpose 중 최소 1개는 필요합니다.")
    public boolean hasAtLeastOne() {
        return receiverType != null || purpose != null;
    }
}
