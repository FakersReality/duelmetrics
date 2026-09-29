package com.alial.duelmetrics.roles;

import com.alial.duelmetrics.model.CardRole;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CardRoleConfig {

    private final Map<String, Set<CardRole>> rolesByName;

    private CardRoleConfig(
            Map<String, Set<CardRole>> rolesByName
    ) {
        this.rolesByName = rolesByName;
    }

    public static void createTemplateIfMissing(
            Path filePath,
            Collection<String> cardNames
    ) throws IOException {

        if (Files.exists(filePath)) {
            return;
        }

        List<String> lines = new ArrayList<>();

        lines.add("# Assign roles using commas.");
        lines.add("# Example:");
        lines.add(
                "# Junk Synchron=STARTER,EXTENDER"
        );
        lines.add(
                "# Allowed roles: "
                        + List.of(CardRole.values())
        );
        lines.add("");

        cardNames.stream()
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(name -> lines.add(name + "="));

        Files.write(
                filePath,
                lines,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW
        );
    }

    public static CardRoleConfig load(Path filePath)
            throws IOException {

        Map<String, Set<CardRole>> rolesByName =
                new HashMap<>();

        List<String> lines = Files.readAllLines(
                filePath,
                StandardCharsets.UTF_8
        );

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int equalsPosition = line.indexOf('=');

            if (equalsPosition < 0) {
                throw new IllegalArgumentException(
                        "Missing = on line " + (index + 1)
                );
            }

            String cardName = line
                    .substring(0, equalsPosition)
                    .trim();

            String roleText = line
                    .substring(equalsPosition + 1)
                    .trim();

            EnumSet<CardRole> roles =
                    EnumSet.noneOf(CardRole.class);

            if (!roleText.isEmpty()) {
                String[] roleNames = roleText.split(",");

                for (String roleName : roleNames) {
                    try {
                        CardRole role = CardRole.valueOf(
                                roleName.trim()
                                        .toUpperCase(Locale.ROOT)
                        );

                        roles.add(role);
                    } catch (IllegalArgumentException exception) {
                        throw new IllegalArgumentException(
                                "Invalid role on line "
                                        + (index + 1)
                                        + ": "
                                        + roleName,
                                exception
                        );
                    }
                }
            }

            rolesByName.put(
                    normalizeName(cardName),
                    roles
            );
        }

        return new CardRoleConfig(rolesByName);
    }

    public CardRole[] rolesFor(String cardName) {
        Set<CardRole> roles = rolesByName.getOrDefault(
                normalizeName(cardName),
                Set.of()
        );

        return roles.toArray(CardRole[]::new);
    }

    public int getAssignedCardCount() {
        int count = 0;

        for (Set<CardRole> roles : rolesByName.values()) {
            if (!roles.isEmpty()) {
                count++;
            }
        }

        return count;
    }

    private static String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}