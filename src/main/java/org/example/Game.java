package org.example;

import java.nio.file.CopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Game implements Comparable<Game> {

    private LocalDate date;
    private Matchup matchup;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd");

    public Game(LocalDate date, Matchup matchup) {
        this.date = date;
        this.matchup = matchup;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Matchup getMatchup() {
        return matchup;
    }

    public void setMatchup(Matchup matchup) {
        this.matchup = matchup;
    }

    @Override
    public String toString() {
        return date.format(FORMATTER) + " - " + matchup.toString();
    }

    @Override
    public int compareTo(Game other) {
        return this.date.compareTo(other.date);
    }
}
