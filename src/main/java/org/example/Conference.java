package org.example;

import java.util.ArrayList;
import java.util.List;

public class Conference {
    public static final int DIVISIONS_PER_CONF = 2;
    private String name;
    private List<Division> divisions = new ArrayList<>(DIVISIONS_PER_CONF);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Division> getDivisions() {
        return divisions;
    }

    public void setDivisions(List<Division> divisions) {
        this.divisions = divisions;
    }
}
