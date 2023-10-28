package com.nfssoundtrack.newapproach.model2;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="series")
public class Series {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "position")
    private Integer position;

    @Column(name = "name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String toRadioString() {
        return "[NFSSoundtrack ID: " + id + "] " + "Series: '" + name + "'";
    }
}
