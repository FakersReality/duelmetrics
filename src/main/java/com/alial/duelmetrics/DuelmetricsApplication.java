package com.alial.duelmetrics;

import com.alial.duelmetrics.analysis.DeckSimulator;
import com.alial.duelmetrics.analysis.HandAnalyzer;
import com.alial.duelmetrics.analysis.SimulationResult;
import com.alial.duelmetrics.api.YgoCardClient;
import com.alial.duelmetrics.api.YgoCardMapper;
import com.alial.duelmetrics.api.dto.YgoCardData;
import com.alial.duelmetrics.importer.YdkDeckData;
import com.alial.duelmetrics.importer.YdkDeckParser;
import com.alial.duelmetrics.model.Card;
import com.alial.duelmetrics.model.CardRole;
import com.alial.duelmetrics.model.Deck;
import com.alial.duelmetrics.roles.CardRoleConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SpringBootApplication
public class DuelmetricsApplication {

    public static void main(String[] args)
            throws IOException {

        ConfigurableApplicationContext context =
                SpringApplication.run(
                        DuelmetricsApplication.class,
                        args
                );

        YdkDeckData deckFile =
                new YdkDeckParser().parse(
                        Path.of("Synchron.ydk")
                );

        YgoCardClient cardClient =
                context.getBean(YgoCardClient.class);

        List<Long> uniqueMainDeckIds =
                deckFile.mainDeckIds()
                        .stream()
                        .distinct()
                        .toList();

        List<YgoCardData> cardData =
                cardClient.getCardsByIds(
                        uniqueMainDeckIds
                );

        Map<Long, YgoCardData> cardsById =
                cardData.stream()
                        .collect(Collectors.toMap(
                                YgoCardData::id,
                                Function.identity()
                        ));

        Path rolesFile =
                Path.of("Synchron-roles.txt");

        List<String> cardNames = cardData.stream()
                .map(YgoCardData::name)
                .toList();

        CardRoleConfig.createTemplateIfMissing(
                rolesFile,
                cardNames
        );

        CardRoleConfig roleConfig =
                CardRoleConfig.load(rolesFile);

        YgoCardMapper cardMapper = new YgoCardMapper();
        Deck synchronDeck = new Deck("Synchron");

        for (long cardId : deckFile.mainDeckIds()) {
            YgoCardData data = requireCard(
                    cardsById,
                    cardId
            );

            Card card = cardMapper.toCard(
                    data,
                    roleConfig.rolesFor(data.name())
            );

            if (!synchronDeck.addCard(card)) {
                throw new IllegalStateException(
                        "Too many copies of "
                                + card.getName()
                );
            }
        }

        System.out.println();
        System.out.println("Synchron deck imported:");
        System.out.println(
                "Main Deck: " + synchronDeck.getSize()
        );
        System.out.println(
                "Extra Deck: "
                        + deckFile.extraDeckIds().size()
        );
        System.out.println(
                "Side Deck: "
                        + deckFile.sideDeckIds().size()
        );
        System.out.println(
                "Valid Main Deck: "
                        + synchronDeck.isValidMainDeck()
        );
        System.out.println(
                "Cards with assigned roles: "
                        + roleConfig.getAssignedCardCount()
        );

        Map<Long, Long> copyCounts =
                deckFile.mainDeckIds()
                        .stream()
                        .collect(Collectors.groupingBy(
                                Function.identity(),
                                LinkedHashMap::new,
                                Collectors.counting()
                        ));

        System.out.println();
        System.out.println("Deck list:");

        for (Map.Entry<Long, Long> entry
                : copyCounts.entrySet()) {

            YgoCardData data = requireCard(
                    cardsById,
                    entry.getKey()
            );

            CardRole[] roles =
                    roleConfig.rolesFor(data.name());

            System.out.println(
                    entry.getValue()
                            + "x "
                            + data.name()
                            + " "
                            + List.of(roles)
            );
        }

        List<Card> openingHand =
                synchronDeck.drawOpeningHand();

        System.out.println();
        System.out.println("Random opening hand:");

        for (Card card : openingHand) {
            System.out.println(
                    "- "
                            + card.getName()
                            + " "
                            + card.getRoles()
            );
        }

        HandAnalyzer analyzer = new HandAnalyzer();
        analyzer.printAnalysis(openingHand);

        DeckSimulator simulator = new DeckSimulator();

        System.out.println();
        System.out.println(
                "10,000-hand probability analysis:"
        );

        for (CardRole role : CardRole.values()) {
            SimulationResult result =
                    simulator.simulate(
                            synchronDeck,
                            role,
                            10_000
                    );

            System.out.printf(
                    "%-15s %.2f%%%n",
                    role.name().replace('_', ' '),
                    result.getProbabilityPercentage()
            );
        }

        if (roleConfig.getAssignedCardCount() == 0) {
            System.out.println();
            System.out.println(
                    "No roles are assigned yet."
            );
            System.out.println(
                    "Edit Synchron-roles.txt, "
                            + "then run the program again."
            );
        }
    }

    private static YgoCardData requireCard(
            Map<Long, YgoCardData> cardsById,
            long cardId
    ) {
        YgoCardData data = cardsById.get(cardId);

        if (data == null) {
            throw new IllegalStateException(
                    "Card was not returned by the API: "
                            + cardId
            );
        }

        return data;
    }
}