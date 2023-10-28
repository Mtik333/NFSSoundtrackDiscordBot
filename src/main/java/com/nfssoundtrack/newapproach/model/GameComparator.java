package com.nfssoundtrack.newapproach.model;

import com.nfssoundtrack.newapproach.model2.Games;

import java.util.Comparator;

public class GameComparator implements Comparator<Games> {

    @Override
    public int compare(Games o1, Games o2) {
        return o1.getId().compareTo(o2.getId());
    }
}
