package com.example.tonefitserver.domain.correction.dto;

import com.example.tonefitserver.domain.session.Purpose;
import com.example.tonefitserver.domain.session.Receiver;

public record DraftSaveRequest(
        Receiver receiverType,
        Purpose purpose,
        String subject,
        String originalEmail
) {
}
