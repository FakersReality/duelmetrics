package com.alial.duelmetrics.api;

import com.alial.duelmetrics.api.dto.YgoCardData;
import com.alial.duelmetrics.api.dto.YgoCardResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class YgoCardClient {

    private final RestClient restClient;

    public YgoCardClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://db.ygoprodeck.com")
                .build();
    }

    public YgoCardData getCardById(long cardId) {
        return getCardsByIds(List.of(cardId)).getFirst();
    }

    public List<YgoCardData> getCardsByIds(
            List<Long> cardIds
    ) {
        if (cardIds == null || cardIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one card ID is required."
            );
        }

        String joinedIds = cardIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        YgoCardResponse response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v7/cardinfo.php")
                        .queryParam("id", joinedIds)
                        .build())
                .retrieve()
                .body(YgoCardResponse.class);

        if (response == null
                || response.data() == null
                || response.data().isEmpty()) {

            throw new IllegalStateException(
                    "No cards were returned by YGOPRODeck."
            );
        }

        return List.copyOf(response.data());
    }
}