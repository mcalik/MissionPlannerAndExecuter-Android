package com.calikmustafa.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Mustafa on 19-Nov-14.
 */
public class Team  implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private Soldier leader;
    private ArrayList<Soldier> teamList;

    public Team(int id, String name, Soldier leader, ArrayList<Soldier> teamList) {
        this.id = id;
        this.name = name;
        this.leader = leader;
        this.teamList = teamList;
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

    public Soldier getLeader() {
        return leader;
    }

    public void setLeader(Soldier leader) {
        this.leader = leader;
    }

    public ArrayList<Soldier> getTeamList() {
        return teamList;
    }

    public void setTeamList(ArrayList<Soldier> teamList) {
        this.teamList = teamList;
    }
}
