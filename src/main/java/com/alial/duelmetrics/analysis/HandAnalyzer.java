package com.alial.duelmetrics.analysis;

import com.alial.duelmetrics.model.Card;
import com.alial.duelmetrics.model.CardRole;

import java.util.List;

public class HandAnalyzer {

    public int countCardsWithRole(
            List<Card> hand,
            CardRole role
    ) {
        int count = 0;

        for (Card card : hand) {
            if (card.hasRole(role)) {
                count++;
            }
        }

        return count;
    }

    public boolean containsRole(
            List<Card> hand,
            CardRole role
    ) {
        return countCardsWithRole(hand, role) > 0;
    }

    public void printAnalysis(List<Card> hand) {
        System.out.println();
        System.out.println("Opening-hand analysis:");

        for (CardRole role : CardRole.values()) {
            int count = countCardsWithRole(hand, role);

            System.out.println(
                    role.name().replace('_', ' ') + ": " + count
            );
        }

        System.out.println(
                "Contains a starter: "
                        + containsRole(hand, CardRole.STARTER)
        );
    }
}