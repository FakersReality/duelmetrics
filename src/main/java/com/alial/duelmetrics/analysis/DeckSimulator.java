package com.alial.duelmetrics.analysis;

import com.alial.duelmetrics.model.Card;
import com.alial.duelmetrics.model.CardRole;
import com.alial.duelmetrics.model.Deck;

import java.util.List;

public class DeckSimulator{
    private final HandAnalyzer analyzer;

    public DeckSimulator(){
        this.analyzer = new HandAnalyzer();
    }

    public SimulationResult simulate(
            Deck deck,
            CardRole role,
            int numberOfSimulations
    ) {
        if (!deck.isValidMainDeck()) {
            throw new IllegalArgumentException(
                    "The deck must contain between 40 and 60 cards."
            );
        }

        if (numberOfSimulations <= 0) {
            throw new IllegalArgumentException(
                    "The number of simulations must be greater than zero."
            );
        }

        int successfulHands = 0;

        for (int simulation = 0;
             simulation < numberOfSimulations;
             simulation++) {

            List<Card> openingHand = deck.drawOpeningHand();

            if (analyzer.containsRole(openingHand, role)) {
                successfulHands++;
            }
        }

        return new SimulationResult(
                role,
                numberOfSimulations,
                successfulHands
        );
    }
}