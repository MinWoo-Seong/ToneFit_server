package com.example.ToneFit.correction.dto;

import com.example.ToneFit.session.model.Purpose;
import com.example.ToneFit.session.model.Receiver;

public record DraftSaveRequest(
        Receiver receiverType,
        Purpose purpose,
        String subject,
        String originalEmail
) {
}
