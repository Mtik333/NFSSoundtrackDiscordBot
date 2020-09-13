package com.nfssoundtrack.newapproach.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class Game implements Serializable, Comparable<Game> {

    private final Integer id;
    private final Series series;
    private final String prefix;
    private final String name;

    public Game(Integer id, Series series, String prefix, String name) {
        this.id = id;
        this.series = series;
        this.prefix = prefix;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public Series getSeries() {
        return series;
    }

    @Override
    public String toString() {
        return "Game{" + "id=" + id + ", series=" + series + ", prefix='"
                + prefix + '\'' + ", name='" + name + '\'' + '}';
    }

    public String toRadioString() {
        return "[NFSSoundtrack ID: " + id + "]" + " '" + prefix + "'" + ": '" + name + '\''
                + " of series: " + series.toRadioString();
    }

    public String getFullGameName() {
        return prefix + " " + name;
    }

    @Override
    public int compareTo(@NotNull Game o) {
        return this.getId().compareTo(o.getId());
    }
}
