package com.nfssoundtrack.newapproach.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class Song implements Serializable, Comparable<Song> {

    private final Integer id;
    private final String band;
    private final String title;
    private final String src_id;
    private final String description;
    private final Game game;

    public Song(Integer id, String band, String title, String src_id, String description, Game game) {
        this.id = id;
        this.band = band;
        this.title = title;
        this.description = description;
        this.src_id = src_id;
        this.game = game;
    }

    public Integer getId() {
        return id;
    }

    public String getBand() {
        return band;
    }

    public String getTitle() {
        return title;
    }

    public String getSrc_id() {
        return src_id;
    }

    public Game getGame() {
        return game;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(id, song.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Song{" + "id=" + id + ", band='" + band + '\'' + ", title='" + title + '\''
                + ", src_id='" + src_id + '\'' + ", description='" + description + '\'';
    }

    public String toRadioString() {
        return "[NFSSoundtrack ID: " + id + "]" + " '" + title + "'" + " by '" + band +
                "' from game: " + game.toRadioString();
    }

    public String toStatusString() {
        String status = title + " by " + band + " from " + game.getFullGameName();
        if (status.length() > 128) {
            return status.substring(0, 127);
        } else return status;
    }

    @Override
    public int compareTo(@NotNull Song o) {
        return this.getId().compareTo(o.getId());
    }
}
