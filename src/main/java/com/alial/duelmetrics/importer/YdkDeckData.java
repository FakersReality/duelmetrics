package com.alial.duelmetrics.importer;

import java.util.List;

public record YdkDeckData(List<Long> mainDeckIds, List<Long> extraDeckIds, List<Long> sideDeckIds){
    public YdkDeckData{
        mainDeckIds = List.copyOf(mainDeckIds);
        extraDeckIds = List.copyOf(extraDeckIds);
        sideDeckIds = List.copyOf(sideDeckIds);
    }
}