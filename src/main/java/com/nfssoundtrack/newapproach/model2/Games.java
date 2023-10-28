package com.nfssoundtrack.newapproach.model2;

import jakarta.persistence.*;

@Entity
@Table(name="games")
public class Games {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;


    @Column(name = "prefix")
    private String prefix;

    @Transient
    private Integer position;

    @Column(name = "gametitle")
    private String gametitle;

    @Column(name = "gameshort")
    private String gameshort;

    @Transient
    private String color;

    @Transient
    private String upperhtml;

    @Transient
    private String bottomhtml;

    @Transient
    private Integer game_status;

    @Transient
    private Integer hidden;

    @OneToOne(optional = false)
    @JoinColumn(name = "series")
    private Series series;

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public Integer getHidden() {
        return hidden;
    }

    public void setHidden(Integer hidden) {
        this.hidden = hidden;
    }

    public Integer getGame_status() {
        return game_status;
    }

    public void setGame_status(Integer game_status) {
        this.game_status = game_status;
    }

    public String getBottomhtml() {
        return bottomhtml;
    }

    public void setBottomhtml(String bottomhtml) {
        this.bottomhtml = bottomhtml;
    }

    public String getUpperhtml() {
        return upperhtml;
    }

    public void setUpperhtml(String upperhtml) {
        this.upperhtml = upperhtml;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getGameshort() {
        return gameshort;
    }

    public void setGameshort(String gameshort) {
        this.gameshort = gameshort;
    }

    public String getGametitle() {
        return gametitle;
    }

    public void setGametitle(String gametitle) {
        this.gametitle = gametitle;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String toRadioString() {
        return "[NFSSoundtrack ID: " + id + "]" + " '" + prefix + "'" + ": '" + gametitle + '\''
                + " of series: " + series.toRadioString();
    }

    public String getFullGameName() {
        return prefix + " " + gametitle;
    }
}
