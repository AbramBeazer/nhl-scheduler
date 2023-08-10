package org.example;

public class Matchup {
    private Team away;
    private Team home;

    public Matchup(Team away, Team home) {
        this.away = away;
        this.home = home;
    }

    public Team getAway() {
        return away;
    }

    public void setAway(Team away) {
        this.away = away;
    }

    public Team getHome() {
        return home;
    }

    public void setHome(Team home) {
        this.home = home;
    }


    @Override
    public String toString() {
        return away.getName() + " @ " + home.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Matchup){
            Matchup matchup = (Matchup) obj;
            return matchup.away.equals(this.away) && matchup.home.equals(this.home);
        }
        return false;
    }
}
