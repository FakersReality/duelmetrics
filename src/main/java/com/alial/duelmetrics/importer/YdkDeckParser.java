package com.alial.duelmetrics.importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class YdkDeckParser {

    private enum Section {
        NONE,
        MAIN,
        EXTRA,
        SIDE
    }

    public YdkDeckData parse(Path filePath)
            throws IOException {

        List<Long> mainDeckIds = new ArrayList<>();
        List<Long> extraDeckIds = new ArrayList<>();
        List<Long> sideDeckIds = new ArrayList<>();

        List<String> lines =
                Files.readAllLines(filePath);

        Section currentSection = Section.NONE;

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();

            if (line.isEmpty()) {
                continue;
            }

            String lowerCaseLine = line.toLowerCase();

            switch (lowerCaseLine) {
                case "#main" -> {
                    currentSection = Section.MAIN;
                    continue;
                }

                case "#extra" -> {
                    currentSection = Section.EXTRA;
                    continue;
                }

                case "!side", "#side" -> {
                    currentSection = Section.SIDE;
                    continue;
                }
            }

            if (line.startsWith("#")
                    || line.startsWith("!")) {
                continue;
            }

            long cardId;

            try {
                cardId = Long.parseLong(line);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        "Invalid card ID on line "
                                + (index + 1)
                                + ": "
                                + line,
                        exception
                );
            }

            switch (currentSection) {
                case MAIN -> mainDeckIds.add(cardId);
                case EXTRA -> extraDeckIds.add(cardId);
                case SIDE -> sideDeckIds.add(cardId);

                case NONE -> throw new IllegalArgumentException(
                        "Card ID appeared before a deck section "
                                + "on line "
                                + (index + 1)
                );
            }
        }

        return new YdkDeckData(
                mainDeckIds,
                extraDeckIds,
                sideDeckIds
        );
    }
}