package com.nfssoundtrack.newapproach.model2;

import jakarta.persistence.*;

@Entity
@Table(name="songs")
public class Songs {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "band")
    private String band;

    @Column(name = "title")
    private String title;

    @Column(name = "src_id")
    private String src_id;

    @Column(name = "info")
    private String info;

    @OneToOne(optional = false)
    @JoinColumn(name = "gameid", nullable = false)
    private Games games;

    public Games getGames() {
        return games;
    }

    public void setGames(Games games) {
        this.games = games;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getSrc_id() {
        return src_id;
    }

    public void setSrc_id(String src_id) {
        this.src_id = src_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBand() {
        return band;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Songs{" +
                "id=" + id +
                ", band='" + band + '\'' +
                ", title='" + title + '\'' +
                ", src_id='" + src_id + '\'' +
                ", info='" + info + '\'' +
                ", games=" + games +
                '}';
    }

    public String toRadioString() {
        return "[NFSSoundtrack ID: " + id + "]" + " '" + title + "'" + " by '" + band +
                "' from game: " + games.toRadioString();
    }

    public String toStatusString() {
        String status = title + " by " + band + " from " + games.getFullGameName();
        if (status.length() > 128) {
            return status.substring(0, 127);
        } else return status;
    }
}
