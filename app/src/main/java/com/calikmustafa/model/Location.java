package com.calikmustafa.model;

import java.io.Serializable;

/**
 * Created by Mustafa on 21-Nov-14.
 */
public class Location  implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int missionID;
    private int soldierID;
    private double latitude;
    private double longitude;
    private String time;
    private String status;

    public Location(int missionID, int soldierID, double latitude, double longitude, String status) {
        this.missionID = missionID;
        this.soldierID = soldierID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }

    public Location(String status, int missionID, int soldierID, double latitude, double longitude, String time) {
        this.status = status;
        this.missionID = missionID;
        this.soldierID = soldierID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMissionID() {
        return missionID;
    }

    public void setMissionID(int missionID) {
        this.missionID = missionID;
    }

    public int getSoldierID() {
        return soldierID;
    }

    public void setSoldierID(int soldierID) {
        this.soldierID = soldierID;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
