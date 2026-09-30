package com.alial.duelmetrics.web;

import com.alial.duelmetrics.analysis.DeckSimulator;
import com.alial.duelmetrics.analysis.SimulationResult;
import com.alial.duelmetrics.api.YgoCardClient;
import com.alial.duelmetrics.api.YgoCardMapper;
import com.alial.duelmetrics.api.dto.YgoCardData;
import com.alial.duelmetrics.importer.YdkDeckData;
import com.alial.duelmetrics.importer.YdkDeckParser;
import com.alial.duelmetrics.model.Card;
import com.alial.duelmetrics.model.CardRole;
import com.alial.duelmetrics.model.Deck;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private static final int SIMULATION_COUNT = 10_000;

    private final YdkDeckParser deckParser =
            new YdkDeckParser();

    private final YgoCardMapper cardMapper =
            new YgoCardMapper();

    private final DeckSimulator deckSimulator =
            new DeckSimulator();

    private final YgoCardClient cardClient;

    public HomeController(YgoCardClient cardClient) {
        this.cardClient = cardClient;
    }

    @GetMapping("/")
    public String showHomePage() {
        return "index";
    }

    @PostMapping("/upload")
    public String uploadDeck(
            @RequestParam("deckFile") MultipartFile deckFile,
            Model model,
            HttpSession session
    ) {
        if (deckFile.isEmpty()) {
            model.addAttribute(
                    "errorMessage",
                    "Select a .ydk deck file first."
            );

            return "index";
        }

        String fileName =
                deckFile.getOriginalFilename();

        if (fileName == null ||
                !fileName.toLowerCase().endsWith(".ydk")) {

            model.addAttribute(
                    "errorMessage",
                    "DuelMetrics only accepts .ydk files."
            );

            return "index";
        }

        Path temporaryFile = null;

        try {
            temporaryFile = Files.createTempFile(
                    "duelmetrics-",
                    ".ydk"
            );

            deckFile.transferTo(temporaryFile);

            YdkDeckData deckData =
                    deckParser.parse(temporaryFile);

            String deckName =
                    removeFileExtension(fileName);

            session.setAttribute(
                    "deckData",
                    deckData
            );

            session.setAttribute(
                    "deckName",
                    deckName
            );

            session.removeAttribute("analysis");
            session.removeAttribute("analyzedDeck");
            session.removeAttribute("selectedRoles");

            return "redirect:/tag-choice";

        } catch (IOException | IllegalArgumentException exception) {
            model.addAttribute(
                    "errorMessage",
                    "Could not read the deck: "
                            + exception.getMessage()
            );

            return "index";

        } finally {
            deleteTemporaryFile(temporaryFile);
        }
    }

    @GetMapping("/tag-choice")
    public String showTagChoice(
            HttpSession session,
            Model model
    ) {
        YdkDeckData deckData =
                getDeckData(session);

        if (deckData == null) {
            return "redirect:/";
        }

        addDeckSummaryToModel(
                deckData,
                session,
                model
        );

        return "tag-choice";
    }

    @PostMapping("/tag-choice")
    public String processTagChoice(
            @RequestParam("choice") String choice,
            HttpSession session
    ) {
        YdkDeckData deckData =
                getDeckData(session);

        if (deckData == null) {
            return "redirect:/";
        }

        if ("yes".equalsIgnoreCase(choice)) {
            return "redirect:/tag-cards";
        }

        AnalysisView analysis =
                createAnalysis(
                        deckData,
                        session,
                        Map.of(),
                        false
                );

        session.setAttribute(
                "analysis",
                analysis
        );

        return "redirect:/breakdown";
    }

    @GetMapping("/tag-cards")
    public String showTagCards(
            HttpSession session,
            Model model
    ) {
        YdkDeckData deckData =
                getDeckData(session);

        if (deckData == null) {
            return "redirect:/";
        }

        List<CardTagView> cards =
                createCardTagViews(deckData);

        model.addAttribute(
                "deckName",
                session.getAttribute("deckName")
        );

        model.addAttribute(
                "cards",
                cards
        );

        model.addAttribute(
                "cardRoles",
                CardRole.values()
        );

        return "tag-cards";
    }

    @PostMapping("/analyze")
    public String analyzeDeck(
            HttpServletRequest request,
            HttpSession session
    ) {
        YdkDeckData deckData =
                getDeckData(session);

        if (deckData == null) {
            return "redirect:/";
        }

        Map<Long, List<CardRole>> selectedRoles =
                readSelectedRoles(
                        request,
                        deckData.mainDeckIds()
                );

        session.setAttribute(
                "selectedRoles",
                selectedRoles
        );

        AnalysisView analysis =
                createAnalysis(
                        deckData,
                        session,
                        selectedRoles,
                        true
                );

        session.setAttribute(
                "analysis",
                analysis
        );

        return "redirect:/breakdown";
    }

    @GetMapping("/breakdown")
    public String showBreakdown(
            HttpSession session,
            Model model
    ) {
        AnalysisView analysis =
                (AnalysisView)
                        session.getAttribute("analysis");

        if (analysis == null) {
            return "redirect:/";
        }

        model.addAttribute(
                "analysis",
                analysis
        );

        return "breakdown";
    }

    @PostMapping("/draw-another")
    public String drawAnotherHand(
            HttpSession session
    ) {
        Deck deck =
                (Deck)
                        session.getAttribute("analyzedDeck");

        AnalysisView currentAnalysis =
                (AnalysisView)
                        session.getAttribute("analysis");

        if (deck == null || currentAnalysis == null) {
            return "redirect:/";
        }

        List<HandCardView> newOpeningHand =
                createOpeningHandView(
                        deck.drawOpeningHand()
                );

        AnalysisView updatedAnalysis =
                new AnalysisView(
                        currentAnalysis.deckName(),
                        currentAnalysis.mainDeckCount(),
                        currentAnalysis.extraDeckCount(),
                        currentAnalysis.sideDeckCount(),
                        currentAnalysis.validMainDeck(),
                        currentAnalysis.rolesAssigned(),
                        currentAnalysis.simulationCount(),
                        currentAnalysis.roleResults(),
                        newOpeningHand,
                        currentAnalysis.mainCards(),
                        currentAnalysis.extraCards(),
                        currentAnalysis.sideCards()
                );

        session.setAttribute(
                "analysis",
                updatedAnalysis
        );

        return "redirect:/breakdown#opening-hand";
    }

    private AnalysisView createAnalysis(
            YdkDeckData deckData,
            HttpSession session,
            Map<Long, List<CardRole>> selectedRoles,
            boolean rolesAssigned
    ) {
        Map<Long, YgoCardData> cardDataById =
                retrieveAllCardData(deckData);

        String deckName =
                (String)
                        session.getAttribute("deckName");

        Deck deck =
                new Deck(deckName);

        for (long cardId : deckData.mainDeckIds()) {
            YgoCardData cardData =
                    requireCardData(
                            cardDataById,
                            cardId
                    );

            List<CardRole> roles =
                    selectedRoles.getOrDefault(
                            cardId,
                            List.of()
                    );

            Card card =
                    cardMapper.toCard(
                            cardData,
                            roles.toArray(
                                    CardRole[]::new
                            )
                    );

            boolean added =
                    deck.addCard(card);

            if (!added) {
                throw new IllegalArgumentException(
                        "The deck contains more than "
                                + "three copies of "
                                + card.getName()
                );
            }
        }

        List<RoleResultView> roleResults =
                new ArrayList<>();

        if (rolesAssigned) {
            for (CardRole role : CardRole.values()) {
                SimulationResult result =
                        deckSimulator.simulate(
                                deck,
                                role,
                                SIMULATION_COUNT
                        );

                roleResults.add(
                        new RoleResultView(
                                formatRole(role),
                                result.getSuccessfulHands(),
                                result.getTotalHands(),
                                result.getProbabilityPercentage()
                        )
                );
            }
        }

        List<HandCardView> openingHand =
                createOpeningHandView(
                        deck.drawOpeningHand()
                );

        List<DeckCardView> mainCards =
                createDeckCardViews(
                        deckData.mainDeckIds(),
                        cardDataById,
                        selectedRoles,
                        true
                );

        List<DeckCardView> extraCards =
                createDeckCardViews(
                        deckData.extraDeckIds(),
                        cardDataById,
                        Map.of(),
                        false
                );

        List<DeckCardView> sideCards =
                createDeckCardViews(
                        deckData.sideDeckIds(),
                        cardDataById,
                        Map.of(),
                        false
                );

        session.setAttribute(
                "analyzedDeck",
                deck
        );

        return new AnalysisView(
                deckName,
                deck.getSize(),
                deckData.extraDeckIds().size(),
                deckData.sideDeckIds().size(),
                deck.isValidMainDeck(),
                rolesAssigned,
                SIMULATION_COUNT,
                List.copyOf(roleResults),
                openingHand,
                mainCards,
                extraCards,
                sideCards
        );
    }

    private Map<Long, List<CardRole>> readSelectedRoles(
            HttpServletRequest request,
            List<Long> mainDeckIds
    ) {
        Map<Long, Integer> quantities =
                countCardQuantities(mainDeckIds);

        Map<Long, List<CardRole>> selectedRoles =
                new LinkedHashMap<>();

        for (long cardId : quantities.keySet()) {
            String parameterName =
                    "roles_" + cardId;

            String[] roleValues =
                    request.getParameterValues(
                            parameterName
                    );

            if (roleValues == null) {
                selectedRoles.put(
                        cardId,
                        List.of()
                );

                continue;
            }

            List<CardRole> roles =
                    new ArrayList<>();

            for (String roleValue : roleValues) {
                roles.add(
                        CardRole.valueOf(roleValue)
                );
            }

            selectedRoles.put(
                    cardId,
                    List.copyOf(roles)
            );
        }

        return selectedRoles;
    }

    private Map<Long, YgoCardData> retrieveAllCardData(
            YdkDeckData deckData
    ) {
        Map<Long, Integer> allCardIds =
                new LinkedHashMap<>();

        addCardIds(
                allCardIds,
                deckData.mainDeckIds()
        );

        addCardIds(
                allCardIds,
                deckData.extraDeckIds()
        );

        addCardIds(
                allCardIds,
                deckData.sideDeckIds()
        );

        List<YgoCardData> cardDataList =
                cardClient.getCardsByIds(
                        new ArrayList<>(
                                allCardIds.keySet()
                        )
                );

        Map<Long, YgoCardData> cardDataById =
                new LinkedHashMap<>();

        for (YgoCardData cardData : cardDataList) {
            cardDataById.put(
                    cardData.id(),
                    cardData
            );
        }

        return cardDataById;
    }

    private void addCardIds(
            Map<Long, Integer> destination,
            List<Long> cardIds
    ) {
        for (long cardId : cardIds) {
            destination.putIfAbsent(
                    cardId,
                    1
            );
        }
    }

    private List<CardTagView> createCardTagViews(
            YdkDeckData deckData
    ) {
        Map<Long, Integer> quantities =
                countCardQuantities(
                        deckData.mainDeckIds()
                );

        List<YgoCardData> cardDataList =
                cardClient.getCardsByIds(
                        new ArrayList<>(
                                quantities.keySet()
                        )
                );

        Map<Long, YgoCardData> cardDataById =
                new LinkedHashMap<>();

        for (YgoCardData cardData : cardDataList) {
            cardDataById.put(
                    cardData.id(),
                    cardData
            );
        }

        List<CardTagView> cardViews =
                new ArrayList<>();

        for (Map.Entry<Long, Integer> entry
                : quantities.entrySet()) {

            YgoCardData cardData =
                    requireCardData(
                            cardDataById,
                            entry.getKey()
                    );

            cardViews.add(
                    new CardTagView(
                            cardData.id(),
                            cardData.name(),
                            cardData.type(),
                            entry.getValue(),
                            findImageUrl(cardData)
                    )
            );
        }

        return List.copyOf(cardViews);
    }

    private List<DeckCardView> createDeckCardViews(
            List<Long> cardIds,
            Map<Long, YgoCardData> cardDataById,
            Map<Long, List<CardRole>> selectedRoles,
            boolean includeRoles
    ) {
        Map<Long, Integer> quantities =
                countCardQuantities(cardIds);

        List<DeckCardView> cardViews =
                new ArrayList<>();

        for (Map.Entry<Long, Integer> entry
                : quantities.entrySet()) {

            long cardId =
                    entry.getKey();

            YgoCardData cardData =
                    requireCardData(
                            cardDataById,
                            cardId
                    );

            String roles = "";

            if (includeRoles) {
                roles = formatRoles(
                        selectedRoles.getOrDefault(
                                cardId,
                                List.of()
                        )
                );
            }

            cardViews.add(
                    new DeckCardView(
                            cardId,
                            cardData.name(),
                            cardData.type(),
                            entry.getValue(),
                            roles
                    )
            );
        }

        return List.copyOf(cardViews);
    }

    private List<HandCardView> createOpeningHandView(
            List<Card> cards
    ) {
        List<HandCardView> handCards =
                new ArrayList<>();

        for (Card card : cards) {
            handCards.add(
                    new HandCardView(
                            card.getName(),
                            card.getType().name(),
                            formatRoles(
                                    new ArrayList<>(
                                            card.getRoles()
                                    )
                            ),
                            card.getImageUrl()
                    )
            );
        }

        return List.copyOf(handCards);
    }

    private Map<Long, Integer> countCardQuantities(
            List<Long> cardIds
    ) {
        Map<Long, Integer> quantities =
                new LinkedHashMap<>();

        for (long cardId : cardIds) {
            quantities.merge(
                    cardId,
                    1,
                    Integer::sum
            );
        }

        return quantities;
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

        return fullImage == null
                ? ""
                : fullImage;
    }

    private YgoCardData requireCardData(
            Map<Long, YgoCardData> cardDataById,
            long cardId
    ) {
        YgoCardData cardData =
                cardDataById.get(cardId);

        if (cardData == null) {
            throw new IllegalStateException(
                    "Card information was not found "
                            + "for ID "
                            + cardId
            );
        }

        return cardData;
    }

    private String formatRoles(
            List<CardRole> roles
    ) {
        if (roles.isEmpty()) {
            return "UNTAGGED";
        }

        List<String> roleNames =
                new ArrayList<>();

        for (CardRole role : roles) {
            roleNames.add(
                    formatRole(role)
            );
        }

        return String.join(
                ", ",
                roleNames
        );
    }

    private String formatRole(CardRole role) {
        return role.name().replace('_', ' ');
    }

    private YdkDeckData getDeckData(
            HttpSession session
    ) {
        return (YdkDeckData)
                session.getAttribute("deckData");
    }

    private void addDeckSummaryToModel(
            YdkDeckData deckData,
            HttpSession session,
            Model model
    ) {
        model.addAttribute(
                "deckName",
                session.getAttribute("deckName")
        );

        model.addAttribute(
                "mainDeckCount",
                deckData.mainDeckIds().size()
        );

        model.addAttribute(
                "extraDeckCount",
                deckData.extraDeckIds().size()
        );

        model.addAttribute(
                "sideDeckCount",
                deckData.sideDeckIds().size()
        );
    }

    private String removeFileExtension(
            String fileName
    ) {
        int extensionPosition =
                fileName.toLowerCase().lastIndexOf(
                        ".ydk"
                );

        if (extensionPosition == -1) {
            return fileName;
        }

        return fileName.substring(
                0,
                extensionPosition
        );
    }

    private void deleteTemporaryFile(
            Path temporaryFile
    ) {
        if (temporaryFile == null) {
            return;
        }

        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException ignored) {
            // The operating system can remove it later.
        }
    }

    public record CardTagView(
            long id,
            String name,
            String type,
            int quantity,
            String imageUrl
    ) {
    }

    public record RoleResultView(
            String role,
            int successfulHands,
            int totalHands,
            double probability
    ) {
    }

    public record HandCardView(
            String name,
            String type,
            String roles,
            String imageUrl
    ) {
    }

    public record DeckCardView(
            long id,
            String name,
            String type,
            int quantity,
            String roles
    ) {
    }

    public record AnalysisView(
            String deckName,
            int mainDeckCount,
            int extraDeckCount,
            int sideDeckCount,
            boolean validMainDeck,
            boolean rolesAssigned,
            int simulationCount,
            List<RoleResultView> roleResults,
            List<HandCardView> openingHand,
            List<DeckCardView> mainCards,
            List<DeckCardView> extraCards,
            List<DeckCardView> sideCards
    ) {
    }
}