package com.calikmustafa.model;

import java.io.Serializable;

/**
 * Created by Mustafa on 18-Nov-14.
 */
public class Mission  implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private int teamID;
    private int teamLeaderID;
    private double latitude;
    private double longitude;
    private String time;
    private String details;

    public Mission(int id, String name, int teamID, int teamLeaderID, double latitude, double longitude, String time, String details) {
        this.id = id;
        this.name = name;
        this.teamID = teamID;
        this.teamLeaderID = teamLeaderID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.details = details;
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

    public int getTeamID() {
        return teamID;
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
    }

    public int getTeamLeaderID() {
        return teamLeaderID;
    }

    public void setTeamLeaderID(int teamLeaderID) {
        this.teamLeaderID = teamLeaderID;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "Mission{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", teamID=" + teamID +
                ", teamLeaderID=" + teamLeaderID +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", time='" + time + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
