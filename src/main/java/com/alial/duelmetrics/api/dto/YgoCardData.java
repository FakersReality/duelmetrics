package com.alial.duelmetrics.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YgoCardData(
        long id,
        String name,
        String type,
        String desc,

        @JsonProperty("card_images")
        List<YgoCardImage> cardImages
) {
}