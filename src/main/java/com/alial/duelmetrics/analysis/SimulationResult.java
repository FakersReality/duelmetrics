package com.alial.duelmetrics.analysis;

import com.alial.duelmetrics.model.CardRole;

public class SimulationResult {

    private final CardRole role;
    private final int totalHands;
    private final int successfulHands;

    public SimulationResult(
            CardRole role,
            int totalHands,
            int successfulHands
    ) {
        this.role = role;
        this.totalHands = totalHands;
        this.successfulHands = successfulHands;
    }

    public CardRole getRole() {
        return role;
    }

    public int getTotalHands() {
        return totalHands;
    }

    public int getSuccessfulHands() {
        return successfulHands;
    }

    public double getProbabilityPercentage() {
        return (successfulHands * 100.0) / totalHands;
    }
}