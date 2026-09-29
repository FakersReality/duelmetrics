package com.alial.duelmetrics.analysis;

import com.alial.duelmetrics.model.Card;
import com.alial.duelmetrics.model.CardRole;
import com.alial.duelmetrics.model.CardType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandAnalyzerTest {

    @Test
    void countsCardsWithRequestedRole() {
        HandAnalyzer analyzer = new HandAnalyzer();

        Card starter = new Card(
                1L,
                "Starter",
                CardType.MONSTER,
                CardRole.STARTER
        );

        Card brick = new Card(
                2L,
                "Brick",
                CardType.MONSTER,
                CardRole.BRICK
        );

        List<Card> hand = List.of(starter, brick);

        assertEquals(
                1,
                analyzer.countCardsWithRole(
                        hand,
                        CardRole.STARTER
                )
        );
    }

    @Test
    void detectsWhenHandHasNoStarter() {
        HandAnalyzer analyzer = new HandAnalyzer();

        Card brick = new Card(
                1L,
                "Brick",
                CardType.MONSTER,
                CardRole.BRICK
        );

        List<Card> hand = List.of(brick);

        assertFalse(
                analyzer.containsRole(hand, CardRole.STARTER)
        );

        assertTrue(
                analyzer.containsRole(hand, CardRole.BRICK)
        );
    }
}