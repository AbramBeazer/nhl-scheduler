package org.example;

import java.time.ZoneId;

public class Team {

    private String location;
    private String name;
    private ZoneId timeZone;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullTeamName() {
        return location + " " + name;
    }

    public ZoneId getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(ZoneId timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
        return getFullTeamName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Team) {
            Team other = (Team) obj;
            return this.location.equals(other.location) && this.name.equals(other.name);
        }
        return false;
    }
}
