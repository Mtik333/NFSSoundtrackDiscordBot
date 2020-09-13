package com.nfssoundtrack.newapproach.model;

import java.util.Comparator;

public class SongComparator implements Comparator<Song> {

    @Override
    public int compare(Song o1, Song o2) {
        return o1.getId().compareTo(o2.getId());
    }
}
