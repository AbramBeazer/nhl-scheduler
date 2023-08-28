package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.example.Conference.DIVISIONS_PER_CONF;
import static org.example.Division.TEAMS_PER_DIVISION;
import static org.example.League.CONFS_PER_LEAGUE;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String FILENAME = "nhl.json";

    private static final Random RANDOM = new Random();

    private static final int YEAR = 2023;
    private static final int NEXT_YEAR = YEAR + 1;
    private static final LocalDate OPENING_DAY = LocalDate.of(YEAR, Month.OCTOBER, 7);
    private static final LocalDate HALLOWEEN = LocalDate.of(YEAR, Month.OCTOBER, 31);
    private static final LocalDate CHRISTMAS_ADAM = LocalDate.of(YEAR, Month.DECEMBER, 23);
    private static final LocalDate CHRISTMAS_EVE = LocalDate.of(YEAR, Month.DECEMBER, 24);
    private static final LocalDate CHRISTMAS = LocalDate.of(YEAR, Month.DECEMBER, 25);
    private static final LocalDate BOXING_DAY = LocalDate.of(YEAR, Month.DECEMBER, 26);
    private static final LocalDate ALL_STAR_BEGIN = OPENING_DAY.plusWeeks(16);
    private static final LocalDate ALL_STAR_END = ALL_STAR_BEGIN.plusDays(6);
    private static final LocalDate SUPER_BOWL_SUNDAY = LocalDate.of(NEXT_YEAR, Month.FEBRUARY, 1)
        .with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.SUNDAY));
    private static final int DIVISION_GAMES_PER_TEAM = 26;
    private static final int CONFERENCE_GAMES_PER_TEAM = 24;
    private static final int INTERCONFERENCE_GAMES_PER_TEAM = 32;
    private static final int TOTAL_GAMES =
        (DIVISION_GAMES_PER_TEAM + CONFERENCE_GAMES_PER_TEAM + INTERCONFERENCE_GAMES_PER_TEAM) * TEAMS_PER_DIVISION
            * DIVISIONS_PER_CONF * CONFS_PER_LEAGUE / 2;

    public static void main(String[] args) {
        try (InputStream stream = Main.class.getClassLoader().getResourceAsStream(FILENAME)) {
            Objects.requireNonNull(stream, "Input stream was null");
            League league = MAPPER.readValue(new String(stream.readAllBytes(), StandardCharsets.UTF_8), League.class);

            List<Matchup> matchups = new ArrayList<>();
            matchups.addAll(getDivisionMatchups(league));
            matchups.addAll(getConferenceMatchups(league));
            matchups.addAll(getInterConferenceMatchups(league));

            List<Team> teams = league.getConferences()
                .stream()
                .flatMap(conference -> conference.getDivisions()
                    .stream()
                    .flatMap(division -> division.getTeams().stream()))
                .collect(
                    Collectors.toList());
            List<Game> scheduledGames = scheduleGames(teams, matchups);

            for (Game game : scheduledGames) {
                System.out.println(game);
            }
            System.out.println("\nTotal games: " + scheduledGames.size());
        } catch (IOException e) {
            LOG.error(e.toString());
        }
    }

    private static List<Game> scheduleGames(List<Team> teams, List<Matchup> matchups) {
        Collections.shuffle(matchups);

        List<Game> scheduledGames = new ArrayList<>();

        Set<Team> playedFourDaysAgo = new HashSet<>();
        Set<Team> playedThreeDaysAgo = new HashSet<>();
        Set<Team> playedTwoDaysAgo = new HashSet<>();
        Set<Team> playedYesterday = new HashSet<>();

        final List<Matchup> reservedMatchups = new ArrayList<>();
        for (int i = 0; i < teams.size(); i = i + 2) {
            final Team away = teams.get(i);
            final Team home = teams.get(i + 1);
            Matchup match = matchups.stream()
                .filter(m -> m.getAway().equals(away) && m.getHome().equals(home))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                    "Should have been able to find matchup between any two teams at start of process."));
            reservedMatchups.add(match);
        }
        if (!matchups.removeAll(reservedMatchups)) {
            throw new RuntimeException("Could not remove reserved matchups from master list.");
        }

        LocalDate date = OPENING_DAY;
        while (!matchups.isEmpty()) {

            List<Matchup> potentialMatchups = getPotentialMatchups(matchups, playedTwoDaysAgo, playedYesterday);
            Set<Team> prioritizedTeams = getPriorityTeams(playedFourDaysAgo, playedTwoDaysAgo, playedYesterday);
            Set<Team> hasPlayedToday = new HashSet<>();
            final int gamesToday = Math.min(getNumberOfGamesToday(date), matchups.size());

            for (int i = 0; i < gamesToday; i++) {

                Matchup matchup = chooseMatchup(potentialMatchups, hasPlayedToday, prioritizedTeams);
                if (matchup == null) {
                    break;
                }
                Game game = new Game(date, matchup);
                scheduledGames.add(game);

                hasPlayedToday.add(matchup.getAway());
                hasPlayedToday.add(matchup.getHome());

                if (!matchups.remove(matchup)) {
                    throw new RuntimeException("Failed to remove matchup from list: " + matchup);
                }

            }

            playedFourDaysAgo = new HashSet<>(playedThreeDaysAgo);
            playedThreeDaysAgo = new HashSet<>(playedTwoDaysAgo);
            playedTwoDaysAgo = new HashSet<>(playedYesterday);
            playedYesterday = new HashSet<>(hasPlayedToday);

            date = date.plusDays(1);
        }

        final LocalDate penultimateDate = date.plusDays(1);
        final LocalDate endOfRegularSeason = date.plusDays(2);
        for (int i = 0; i < reservedMatchups.size(); i = i + 2) {
            scheduledGames.add(new Game(penultimateDate, reservedMatchups.get(i)));
        }
        for (int i = 1; i < reservedMatchups.size(); i = i + 2) {
            scheduledGames.add(new Game(endOfRegularSeason, reservedMatchups.get(i)));
        }

        return scheduledGames;
    }

    private static Matchup chooseMatchup(
        List<Matchup> potentialMatchups,
        Set<Team> hasPlayedToday,
        Set<Team> prioritizedTeams) {
        return potentialMatchups.stream()
            .filter(m -> noGamesOnDay(m, hasPlayedToday))
            .min((m1, m2) -> sortMatchups(m1, m2, prioritizedTeams))
            .orElse(null);
    }

    private static List<Matchup> getPotentialMatchups(
        List<Matchup> matchups,
        Set<Team> playedTwoDaysAgo,
        Set<Team> playedYesterday) {
        return matchups.stream()
            .filter(m -> !(playedTwoDaysAgo.contains(m.getAway()) && playedYesterday.contains(m.getAway()))
                && !(playedTwoDaysAgo.contains(m.getHome()) && playedYesterday.contains(m.getHome())))
            .collect(Collectors.toList());
    }

    private static Set<Team> getPriorityTeams(
        Set<Team> playedFourDaysAgo,
        Set<Team> playedTwoDaysAgo,
        Set<Team> playedYesterday) {
        return playedFourDaysAgo.stream()
            .filter(team -> !(playedTwoDaysAgo.contains(team) || playedYesterday.contains(team)))
            .collect(
                Collectors.toSet());
    }

    private static int sortMatchups(Matchup m1, Matchup m2, Set<Team> prioritizedTeams) {
        int sum1 = 0;
        int sum2 = 0;
        if (prioritizedTeams.contains(m1.getAway())) {
            sum1++;
        }
        if (prioritizedTeams.contains(m1.getHome())) {
            sum1++;
        }
        if (prioritizedTeams.contains(m2.getAway())) {
            sum2++;
        }
        if (prioritizedTeams.contains(m2.getHome())) {
            sum2++;
        }
        return sum1 - sum2;
    }

    private static boolean noGamesOnDay(Matchup matchup, Set<Team> teamsForToday) {
        return !(teamsForToday.contains(matchup.getAway())
            || teamsForToday.contains(matchup.getHome()));
    }

    private static int getNumberOfGamesToday(LocalDate date) {
        int games = 0;
        if (date.equals(OPENING_DAY)) {
            games = 3;
        } else if (date.equals(HALLOWEEN) || date.equals(SUPER_BOWL_SUNDAY)) {
            games = 2;
        } else if ((date.equals(CHRISTMAS_ADAM) && BOXING_DAY.getDayOfWeek().equals(DayOfWeek.SATURDAY))
            || date.equals(CHRISTMAS)
            || date.equals(CHRISTMAS_EVE)
            || (date.equals(BOXING_DAY) && !BOXING_DAY.getDayOfWeek().equals(DayOfWeek.SATURDAY))
            || (date.isAfter(ALL_STAR_BEGIN) && date.isBefore(ALL_STAR_END))) {
            games = 0;
        } else if (date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            games = randomBetweenInclusive(3, 7);
        } else if (date.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
            games = randomBetweenInclusive(3, 8);
        } else if (date.getDayOfWeek().equals(DayOfWeek.TUESDAY)) {
            games = randomBetweenInclusive(7, 14);
        } else if (date.getDayOfWeek().equals(DayOfWeek.WEDNESDAY)) {
            games = randomBetweenInclusive(0, 10);
        } else if (date.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
            games = randomBetweenInclusive(7, 14);
        } else if (date.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
            games = randomBetweenInclusive(0, 10);
        } else if (date.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            games = randomBetweenInclusive(7, 14);
        }

        if (Month.APRIL.equals(date.getMonth())) {
            return games + 2;
        } else {
            return games;
        }
    }

    private static List<Matchup> getDivisionMatchups(League league) {
        List<Matchup> matchups = new ArrayList<>();
        for (Conference conference : league.getConferences()) {

            for (Division division : conference.getDivisions()) {
                List<Team> teams = division.getTeams();

                for (int i = 0; i < teams.size(); i++) {

                    for (int j = 0; j < teams.size(); j++) {

                        if (i != j) {
                            matchups.add(new Matchup(teams.get(i), teams.get(j)));
                            //Each team plays 26 intra-division games: 4 against 5 teams and 3 against the other two teams in the division.
                            if ((i + 1) % TEAMS_PER_DIVISION != j) {
                                matchups.add(new Matchup(teams.get(i), teams.get(j)));
                            }
                        }
                    }
                }
            }
        }
        return matchups;
    }

    private static List<Matchup> getConferenceMatchups(League league) {
        List<Matchup> matchups = new ArrayList<>();
        for (Conference conf : league.getConferences()) {

            for (Division division : conf.getDivisions()) {

                for (Division otherDivision : conf.getDivisions()) {

                    if (division != otherDivision) {
                        List<Team> teams = division.getTeams();
                        List<Team> otherTeams = otherDivision.getTeams();
                        for (int i = 0; i < teams.size(); i++) {

                            for (int j = 0; j < otherTeams.size(); j++) {
                                if (i != j) {
                                    matchups.add(new Matchup(teams.get(i), otherTeams.get(j)));
                                    //Each team plays 3 games against the eight teams in the other division of the same conference. As each team must play an even number of home and away games, they will play half of those teams 1-home, 2-away, and the other half 2-home, 1-away.
                                    if ((i + 1) % TEAMS_PER_DIVISION != j && (i + 2) % TEAMS_PER_DIVISION != j) {
                                        matchups.add(new Matchup(teams.get(i), otherTeams.get(j)));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return matchups;
    }

    private static List<Matchup> getInterConferenceMatchups(League league) {
        List<Matchup> matchups = new ArrayList<>();
        for (Conference conf : league.getConferences()) {

            for (Conference otherConf : league.getConferences()) {

                if (conf != otherConf) {

                    List<Team> teams = conf.getDivisions()
                        .stream()
                        .flatMap(division -> division.getTeams().stream())
                        .collect(
                            Collectors.toList());

                    List<Team> otherTeams = otherConf.getDivisions()
                        .stream()
                        .flatMap(division -> division.getTeams().stream())
                        .collect(
                            Collectors.toList());

                    for (Team team : teams) {
                        for (Team otherTeam : otherTeams) {
                            matchups.add(new Matchup(team, otherTeam));
                        }
                    }
                }
            }
        }
        return matchups;
    }

    private static int randomBetweenInclusive(int least, int greatest) {
        return RANDOM.nextInt((greatest + 1) - least) + least;
    }
}