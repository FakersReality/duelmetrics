package com.alial.duelmetrics.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeckTest {

    @Test
    void deckAllowsThreeCopiesOfTheSameCard() {
        Deck deck = new Deck("Test Deck");

        Card darkMagician = new Card(
                46986414L,
                "Dark Magician",
                CardType.MONSTER
        );

        assertTrue(deck.addCard(darkMagician));
        assertTrue(deck.addCard(darkMagician));
        assertTrue(deck.addCard(darkMagician));

        assertEquals(3, deck.countCopies(46986414L));
    }

    @Test
    void deckRejectsFourthCopyOfTheSameCard() {
        Deck deck = new Deck("Test Deck");

        Card darkMagician = new Card(
                46986414L,
                "Dark Magician",
                CardType.MONSTER
        );

        deck.addCard(darkMagician);
        deck.addCard(darkMagician);
        deck.addCard(darkMagician);

        boolean fourthCopyAdded = deck.addCard(darkMagician);

        assertFalse(fourthCopyAdded);
        assertEquals(3, deck.getSize());
    }

    @Test
    void fortyCardDeckIsValid() {
        Deck deck = createFortyCardDeck();

        assertEquals(40, deck.getSize());
        assertTrue(deck.isValidMainDeck());
    }

    @Test
    void deckWithFewerThanFortyCardsIsInvalid() {
        Deck deck = new Deck("Invalid Deck");

        deck.addCard(new Card(
                1L,
                "Test Card",
                CardType.MONSTER
        ));

        assertFalse(deck.isValidMainDeck());
    }

    @Test
    void openingHandContainsFiveCards() {
        Deck deck = createFortyCardDeck();

        List<Card> openingHand = deck.drawOpeningHand();

        assertEquals(5, openingHand.size());
        assertEquals(40, deck.getSize());
    }

    @Test
    void drawingFromSmallDeckThrowsException() {
        Deck deck = new Deck("Small Deck");

        deck.addCard(new Card(
                1L,
                "Only Card",
                CardType.MONSTER
        ));

        assertThrows(
                IllegalStateException.class,
                () -> deck.drawOpeningHand()
        );
    }

    private Deck createFortyCardDeck() {
        Deck deck = new Deck("Forty Card Deck");

        for (long id = 1; id <= 40; id++) {
            deck.addCard(new Card(
                    id,
                    "Test Card " + id,
                    CardType.MONSTER
            ));
        }

        return deck;
    }
}