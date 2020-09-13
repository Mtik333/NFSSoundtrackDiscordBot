package com.nfssoundtrack.newapproach.others;

import com.nfssoundtrack.newapproach.model.*;
import org.apache.commons.collections4.IterableUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SongHelper {

    private static final Logger logger = Logger.getLogger(SongHelper.class.getName());

    private static List<String> getFileLines(String filePath) {
        logger.log(Level.INFO, "Started method loadFile");
        Path path;
        List<String> lines = new ArrayList<>();
        try {
            path = Paths.get(Objects.requireNonNull(SongHelper.class.getClassLoader()
                    .getResource(filePath)).toURI());
            lines = Files.readAllLines(path);
            logger.log(Level.INFO, "Path to input file: " + path.toString());
        } catch (Exception exp) {
            logger.log(Level.WARNING, "Exception: " + exp.getMessage() + ", trying JAR mode");
            try {
                InputStream in = SongHelper.class.getResourceAsStream("/" + filePath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                lines = reader.lines().collect(Collectors.toList());
            } catch (Exception ex2p) {
                logger.log(Level.SEVERE, "Can't do it.");
            }
        }
        logger.log(Level.INFO, "Number of lines: " + lines.size());
        return lines;
    }

    private static String[] cleanedUpLine(String line) {
        logger.log(Level.INFO, "Line text: " + line);
        String skipStartEndChars = line.substring(1, line.length() - 2);
        String[] splitByComma = skipStartEndChars.split(",");
        for (int i = 0; i < splitByComma.length; i++) {
            String value = splitByComma[i].trim().replaceAll("'$", "").replaceFirst("^'", "");
            logger.log(Level.INFO, "Value from line is: " + value);
            splitByComma[i] = value;
        }
        return splitByComma;
    }

    public static List<Series> loadSeriesFile() {
        logger.log(Level.INFO, "Trying to load all series from 'series.sql' file");
        List<Series> allSeries = new ArrayList<>();
        List<String> lines = getFileLines("series.sql");
        for (String line : lines) {
            String[] lineToPersist = cleanedUpLine(line);
            Series series = createSeries(lineToPersist);
            logger.log(Level.INFO, "Series created is: " + series.toRadioString());
            allSeries.add(series);
        }
        allSeries.sort(new SeriesComparator());
        return allSeries;
    }

    public static List<Game> loadGamesFile(List<Series> series) {
        logger.log(Level.INFO, "Trying to load all games from 'games.sql' file");
        List<Game> allGames = new ArrayList<>();
        List<String> lines = getFileLines("games.sql");
        for (String line : lines) {
            String[] lineToPersist = cleanedUpLine(line);
            Game game = createGame(lineToPersist, series);
            logger.log(Level.INFO, "Game created is: " + game.toRadioString());
            allGames.add(game);
        }
        allGames.sort(new GameComparator());
        return allGames;
    }

    public static List<Song> loadSongFile(List<Game> games) {
        logger.log(Level.INFO, "Trying to load all songs from 'songs.sql' file");
        List<Song> allSongs = new ArrayList<>();
        List<String> lines = getFileLines("songs.sql");
        for (String line : lines) {
            String[] lineToPersist = cleanedUpLine(line);
            Song song = createSong(lineToPersist, games);
            logger.log(Level.INFO, "Song loaded is: " + song.toRadioString());
            allSongs.add(song);
        }
        allSongs.sort(new SongComparator());
        return allSongs;
    }

    public static Series createSeries(String[] values) {
        logger.log(Level.INFO, "Started method createSeries: parameters: values: " + Arrays.toString(values));
        Integer id = Integer.valueOf(values[0]);
        String name = values[1].replace("\\", "");
        return new Series(id, name);
    }

    public static Game createGame(String[] values, List<Series> series) {
        logger.log(Level.INFO, "Started method createGame: parameters: values: " + Arrays.toString(values) + ", series: " + series);
        Integer id = Integer.valueOf(values[0]);
        Series serie = IterableUtils.find(series, object -> object.getId().equals(Integer.valueOf(values[1])));
        String prefix = values[2].replace("\\", "");
        String name = values[3].replace("\\", "");
        return new Game(id, serie, prefix, name);
    }

    public static Song createSong(String[] values, List<Game> games) {
        logger.log(Level.INFO, "Started method createSong: parameters: values: " + Arrays.toString(values) + ", games: " + games);
        Integer id = Integer.valueOf(values[0]);
        String band = values[1].replace("\\", "");
        String title = values[2].replace("\\", "");
        String src_id = values[3];
        String description = values[4].replace("\\", "");
        Game game = IterableUtils.find(games, object -> object.getId().equals(Integer.valueOf(values[5])));
        return new Song(id, band, title, src_id, description, game);
    }

    public static List<Song> filterSongs(List<Song> allSongs) {
        logger.log(Level.INFO, "Entering filterSongs, parameters: allSongs: " + allSongs);
        List<Song> filteringResult = new ArrayList<>();
        String seriesIds = MiscHelper.propertyValues.getProperty("series.ids");
        String gamesIds = MiscHelper.propertyValues.getProperty("games.ids");
        String songsIds = MiscHelper.propertyValues.getProperty("songs.ids");
        logger.log(Level.INFO, "Value of parameters: series.ids: " + seriesIds
                + ", games.ids: " + gamesIds + ", songs.ids: " + songsIds);
        if (seriesIds.isEmpty() && gamesIds.isEmpty() && songsIds.isEmpty()) {
            logger.log(Level.INFO, "All songs will be played as no filtering was applied");
            return allSongs;
        }
        if (!seriesIds.isEmpty()) {
            List<Integer> acceptedSeries =
                    Stream.of(seriesIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
            filteringResult.addAll(allSongs.stream().filter(song ->
                    acceptedSeries.contains(song.getGame().getSeries().getId())).collect(Collectors.toList()));
            logger.log(Level.INFO, "Filtered series: " + acceptedSeries);
        }
        if (!gamesIds.isEmpty()) {
            List<Integer> acceptedGames =
                    Stream.of(gamesIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
            filteringResult.addAll(allSongs.stream().filter(song ->
                    acceptedGames.contains(song.getGame().getId())).collect(Collectors.toList()));
            logger.log(Level.INFO, "Filtered games: " + gamesIds);
        }
        if (!songsIds.isEmpty()) {
            List<Integer> acceptedSongs =
                    Stream.of(songsIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
            filteringResult.addAll(allSongs.stream().filter(song ->
                    acceptedSongs.contains(song.getId())).collect(Collectors.toList()));
            logger.log(Level.INFO, "Filtered songs: " + songsIds);
        }
        filteringResult.sort(new SongComparator());
        logger.log(Level.INFO, "Size of filtered songs list: " + filteringResult.size());
        return filteringResult;
    }

    public static Song getRandomSong(List<Song> songsFromFile) {
        int randomValue = ThreadLocalRandom.current().nextInt(0,
                songsFromFile.size());
        logger.log(Level.INFO, "Integer: " + randomValue);
        Song foundSong = songsFromFile.get(randomValue);
        logger.log(Level.INFO, "Selected song: " + foundSong.toRadioString());
        return foundSong;
    }

    public static Song findSongBySrcId(String srcId, List<Song> songs) {
        logger.log(Level.INFO, "YouTube ID: " + srcId);
        Optional<Song> result;
        result = songs.stream().filter(song -> song.getSrc_id().contentEquals(srcId)).findFirst();
        return result.orElse(null);
    }

    public static Song findSongById(Integer id, List<Song> songs) {
        logger.log(Level.INFO, "NFSSoundtrack ID: " + id);
        Optional<Song> result;
        result = songs.stream().filter(song -> song.getId().equals(id)).findFirst();
        return result.orElse(null);
    }

    public static List<Song> findSongsByTitle(String title, List<Song> songs) {
        logger.log(Level.INFO, "Title: " + title);
        List<Song> filteredSongs = songs.stream().filter(song -> song.getTitle().toLowerCase().contentEquals(title))
                .collect(Collectors.toList());
        logger.log(Level.INFO, "Found songs? " + filteredSongs.size());
        return filteredSongs;
    }

    public static List<Song> findSongsByGame(String game, List<Song> songs) {
        logger.log(Level.INFO, "Game: " + game);
        List<Song> filteredSongs = songs.stream().filter(song -> song.getGame().getFullGameName().toLowerCase().contentEquals(game))
                .collect(Collectors.toList());
        logger.log(Level.INFO, "Found songs? " + filteredSongs.size());
        return filteredSongs;
    }

    public static List<Song> findSongsByBand(String band, List<Song> songs) {
        logger.log(Level.INFO, "Band: " + band);
        List<Song> filteredSongs = songs.stream().filter(song -> song.getBand().toLowerCase().contentEquals(band))
                .collect(Collectors.toList());
        logger.log(Level.INFO, "Found songs? " + filteredSongs.size());
        return filteredSongs;
    }
}
