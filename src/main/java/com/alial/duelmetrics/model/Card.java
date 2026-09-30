package com.alial.duelmetrics.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class Card {

    private final long id;
    private final String name;
    private final CardType type;
    private final String imageUrl;
    private final Set<CardRole> roles;

    public Card(
            long id,
            String name,
            CardType type,
            CardRole... roles
    ) {
        this(
                id,
                name,
                type,
                "",
                roles
        );
    }

    public Card(
            long id,
            String name,
            CardType type,
            String imageUrl,
            CardRole... roles
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.imageUrl = imageUrl;
        this.roles = EnumSet.noneOf(CardRole.class);

        Collections.addAll(this.roles, roles);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CardType getType() {
        return type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Set<CardRole> getRoles() {
        return Set.copyOf(roles);
    }

    public boolean hasRole(CardRole role) {
        return roles.contains(role);
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", roles=" + roles +
                '}';
    }
}