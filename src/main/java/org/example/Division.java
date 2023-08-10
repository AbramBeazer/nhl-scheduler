package org.example;

import java.util.ArrayList;
import java.util.List;

public class Division {
    public static final int TEAMS_PER_DIVISION = 8;
    private String name;
    private List<Team> teams = new ArrayList<>(TEAMS_PER_DIVISION);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        if(teams.size() > TEAMS_PER_DIVISION) {
            throw new RuntimeException("Division cannot have more than " + TEAMS_PER_DIVISION + " teams.");
        }
        this.teams = teams;
    }
}
