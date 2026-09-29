package com.alial.duelmetrics.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    private static final int MINIMUM_DECK_SIZE = 40;
    private static final int MAXIMUM_DECK_SIZE = 60;
    private static final int MAXIMUM_COPIES = 3;
    private static final int OPENING_HAND_SIZE = 5;

    private final String name;
    private final List<Card> cards;

    public Deck(String name) {
        this.name = name;
        this.cards = new ArrayList<>();
    }

    public boolean addCard(Card card) {
        if (countCopies(card.getId()) >= MAXIMUM_COPIES) {
            return false;
        }

        cards.add(card);
        return true;
    }

    public int countCopies(long cardId) {
        int count = 0;

        for (Card card : cards) {
            if (card.getId() == cardId) {
                count++;
            }
        }

        return count;
    }

    public List<Card> drawOpeningHand() {
        if (cards.size() < OPENING_HAND_SIZE) {
            throw new IllegalStateException(
                    "The deck needs at least five cards."
            );
        }

        List<Card> shuffledDeck = new ArrayList<>(cards);
        Collections.shuffle(shuffledDeck);

        return new ArrayList<>(
                shuffledDeck.subList(0, OPENING_HAND_SIZE)
        );
    }

    public int getSize() {
        return cards.size();
    }

    public boolean isValidMainDeck() {
        return getSize() >= MINIMUM_DECK_SIZE
                && getSize() <= MAXIMUM_DECK_SIZE;
    }

    public void printDeck() {
        System.out.println("Deck: " + name);
        System.out.println("Number of cards: " + getSize());
        System.out.println(
                "Valid Main Deck: " + (isValidMainDeck() ? "YES" : "NO")
        );

        for (Card card : cards) {
            System.out.println("- " + card.getName()
                    + " [" + card.getType() + "]");
        }
    }
}