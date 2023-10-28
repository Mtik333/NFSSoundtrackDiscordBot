package com.nfssoundtrack.newapproach.others;

import com.nfssoundtrack.newapproach.model.GameComparator;
import com.nfssoundtrack.newapproach.model.SeriesComparator;
import com.nfssoundtrack.newapproach.model.SongComparator;
import com.nfssoundtrack.newapproach.model2.Games;
import com.nfssoundtrack.newapproach.model2.Series;
import com.nfssoundtrack.newapproach.model2.Songs;
import org.apache.commons.collections4.IterableUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
            logger.log(Level.INFO, "Path to input file: " + path);
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

    private static Path getPath(String filePath) {
        logger.log(Level.INFO, "Started method getPath");
        Path path;
        try {
            path = Paths.get(Objects.requireNonNull(SongHelper.class.getClassLoader()
                    .getResource(filePath)).toURI());
            logger.log(Level.INFO, "Path to input file: " + path);
        } catch (Exception exp) {
            logger.log(Level.WARNING, "Exception: " + exp.getMessage() + ", trying JAR mode");
            try {
                //jar path gonna be like jar:file:/app/build/libs/NFSSoundtrack-Radio-0.9.jar!/BOOT-INF/classes!/
                String initialPath = SongHelper.class.getClassLoader().getResource("").getPath();
                initialPath = initialPath.substring(initialPath.indexOf(File.separator),
                        initialPath.indexOf("!")) + File.separator + filePath;
                logger.severe("initialpath " + initialPath);
                throw new Exception("KURWA");
            } catch (Exception ex2p) {
                File file = new File("/app/src/main/resources/" + filePath);
                path = Paths.get(file.toURI());
                logger.log(Level.SEVERE, "Can't do it.");
            }
        }
        logger.log(Level.INFO, "path: " + path);
        return path;
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

    public static List<Songs> filterSongs(List<Songs> allSongs, String seriesIds,
                                          String gamesIds, String songsIds) {
        logger.log(Level.INFO, "Entering filterSongs, parameters: allSongs: " + allSongs);
        List<Songs> filteringResult=new ArrayList<>();
        if (seriesIds == null) {
            String propertiesIds = MiscHelper.propertyValues.getProperty("series.ids");
            if (propertiesIds!=null && !propertiesIds.isEmpty()){
                seriesIds = "series.ids:"+MiscHelper.propertyValues.getProperty("series.ids");
            }
        }
        if (gamesIds == null) {
            String propertiesIds = MiscHelper.propertyValues.getProperty("games.ids");
            if (propertiesIds!=null && !propertiesIds.isEmpty()){
                gamesIds = "games.ids:"+MiscHelper.propertyValues.getProperty("games.ids");
            }
        }
        if (songsIds == null) {
            String propertiesIds = MiscHelper.propertyValues.getProperty("songs.ids");
            if (propertiesIds!=null && !propertiesIds.isEmpty()){
                gamesIds = "songs.ids:"+MiscHelper.propertyValues.getProperty("songs.ids");
            }
        }
        logger.log(Level.INFO, "Value of parameters: series.ids: " + seriesIds
                + ", games.ids: " + gamesIds + ", songs.ids: " + songsIds);
        if (seriesIds==null && gamesIds==null && songsIds==null) {
            logger.log(Level.INFO, "All songs will be played as no filtering was applied");
            return allSongs;
        } else {
            filteringResult = Queries.getSongsByFilter(seriesIds, gamesIds, songsIds);
        }
        if (seriesIds!=null && !seriesIds.isEmpty()) {
            List<Integer> acceptedSeries =
                    Stream.of(seriesIds.split(":")[1].split(","))
                            .map(s -> Integer.parseInt(s.trim())).toList();
            logger.log(Level.INFO, "Filtered series: " + acceptedSeries);
        }
        if (gamesIds!=null && !gamesIds.isEmpty()) {
            List<Integer> acceptedGames =
                    Stream.of(gamesIds.split(":")[1].split(","))
                            .map(s -> Integer.parseInt(s.trim())).toList();
            logger.log(Level.INFO, "Filtered games: " + gamesIds);
        }
        if (songsIds!=null && !songsIds.isEmpty()) {
            List<Integer> acceptedSongs =
                    Stream.of(songsIds.split(":")[1].split(","))
                            .map(s -> Integer.parseInt(s.trim())).toList();
            logger.log(Level.INFO, "Filtered songs: " + songsIds);
        }
        filteringResult.sort(new SongComparator());
        logger.log(Level.INFO, "Size of filtered songs list: " + filteringResult.size());
        return filteringResult;
    }

    public static Songs findSongById(Integer id, List<Songs> songs) {
        logger.log(Level.INFO, "NFSSoundtrack ID: " + id);
        Optional<Songs> result;
        result = songs.stream().filter(song -> song.getId().equals(id)).findFirst();
        return result.orElse(null);
    }

    public static List<Songs> findSongsByTitle(String title, List<Songs> songs) {
        logger.log(Level.INFO, "Title: " + title);
        List<Songs> filteredSongs = songs.stream().filter(song -> song.getTitle().toLowerCase().contentEquals(title))
                .collect(Collectors.toList());
        logger.log(Level.INFO, "Found songs? " + filteredSongs.size());
        return filteredSongs;
    }

    public static List<Songs> findSongsByGame(String game, List<Songs> songs) {
        logger.log(Level.INFO, "Game: " + game);
        List<Songs> filteredSongs = songs.stream().filter(song -> song.getGames().getFullGameName().toLowerCase().contentEquals(game))
                .collect(Collectors.toList());
        logger.log(Level.INFO, "Found songs? " + filteredSongs.size());
        return filteredSongs;
    }

    public static List<Songs> findSongsByBand(String band, List<Songs> songs) {
        logger.log(Level.INFO, "Band: " + band);
        List<Songs> filteredSongs = songs.stream().filter(song -> song.getBand().toLowerCase().contentEquals(band))
                .collect(Collectors.toList());
        logger.log(Level.INFO, "Found songs? " + filteredSongs.size());
        return filteredSongs;
    }
}
