package com.alial.duelmetrics.api.dto;

import java.util.List;

public record YgoCardResponse(
        List<YgoCardData> data
) {
}