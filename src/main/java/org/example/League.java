package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class League {
    public static final int CONFS_PER_LEAGUE = 2;
    private String name;
    private List<Conference> conferences = new ArrayList<>(CONFS_PER_LEAGUE);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Conference> getConferences() {
        return conferences;
    }

    public void setConferences(List<Conference> conferences) {
        this.conferences = conferences;
    }
}
