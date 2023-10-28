package com.nfssoundtrack.newapproach.model;

import com.nfssoundtrack.newapproach.model2.Songs;

import java.util.Comparator;

public class SongComparator implements Comparator<Songs> {

    @Override
    public int compare(Songs o1, Songs o2) {
        return o1.getId().compareTo(o2.getId());
    }
}
