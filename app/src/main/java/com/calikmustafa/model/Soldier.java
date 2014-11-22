package com.calikmustafa.model;

import java.io.Serializable;

/**
 * Created by Mustafa on 17-Nov-14.
 */
public class Soldier implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String rank;
    private String serial;

    public Soldier(int id, String name, String rank, String serial) {
        this.id = id;
        this.name = name;
        this.rank = rank;
        this.serial = serial;
    }

    public Soldier(int id, String name, String rank) {
        this.id = id;
        this.name = name;
        this.rank = rank;
    }

    public Soldier(String name) {
        this.name = name;
    }

    public Soldier() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    @Override
    public String toString() {
        return "Soldier{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", rank='" + rank + '\'' +
                ", serial='" + serial + '\'' +
                '}';
    }
}
