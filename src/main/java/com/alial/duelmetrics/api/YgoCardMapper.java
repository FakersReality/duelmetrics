package com.alial.duelmetrics.api;

import com.alial.duelmetrics.api.dto.YgoCardData;
import com.alial.duelmetrics.model.Card;
import com.alial.duelmetrics.model.CardRole;
import com.alial.duelmetrics.model.CardType;

public class YgoCardMapper {

    public Card toCard(
            YgoCardData cardData,
            CardRole... roles
    ) {
        CardType cardType =
                convertCardType(cardData.type());

        String imageUrl =
                findImageUrl(cardData);

        return new Card(
                cardData.id(),
                cardData.name(),
                cardType,
                imageUrl,
                roles
        );
    }

    private String findImageUrl(
            YgoCardData cardData
    ) {
        if (cardData.cardImages() == null ||
                cardData.cardImages().isEmpty()) {

            return "";
        }

        String smallImage =
                cardData.cardImages()
                        .getFirst()
                        .imageUrlSmall();

        if (smallImage != null &&
                !smallImage.isBlank()) {

            return smallImage;
        }

        String fullImage =
                cardData.cardImages()
                        .getFirst()
                        .imageUrl();

        return fullImage == null ? "" : fullImage;
    }

    private CardType convertCardType(String apiType) {
        if (apiType.contains("Spell")) {
            return CardType.SPELL;
        }

        if (apiType.contains("Trap")) {
            return CardType.TRAP;
        }

        return CardType.MONSTER;
    }
}