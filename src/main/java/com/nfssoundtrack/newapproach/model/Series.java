package com.nfssoundtrack.newapproach.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class Series implements Serializable, Comparable<Series> {

    private final Integer id;
    private final String name;

    public Series(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }


    @Override
    public String toString() {
        return "Series{" + "id=" + id + ", name='" + name + '\'' + '}';
    }

    public String toRadioString() {
        return "[NFSSoundtrack ID: " + id + "] " + "Series: '" + name + "'";
    }

    @Override
    public int compareTo(@NotNull Series o) {
        return this.getId().compareTo(o.getId());
    }
}
