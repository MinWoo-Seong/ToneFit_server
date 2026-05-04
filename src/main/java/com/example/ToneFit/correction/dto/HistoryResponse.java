package com.example.ToneFit.correction.dto;

import java.util.List;

public record HistoryResponse(
        int total,
        int page,
        int size,
        List<SessionSummary> sessions
) {
}
