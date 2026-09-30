package com.alial.duelmetrics.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YgoCardImage(
        @JsonProperty("image_url")
        String imageUrl,

        @JsonProperty("image_url_small")
        String imageUrlSmall,

        @JsonProperty("image_url_cropped")
        String imageUrlCropped
) {
}