package com.nfssoundtrack.newapproach.model;

import com.nfssoundtrack.newapproach.model2.Series;

import java.util.Comparator;

public class SeriesComparator implements Comparator<Series> {

    @Override
    public int compare(Series o1, Series o2) {
        return o1.getId().compareTo(o2.getId());
    }
}
