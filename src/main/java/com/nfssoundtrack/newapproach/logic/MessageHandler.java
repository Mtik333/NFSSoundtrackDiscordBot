package com.nfssoundtrack.newapproach.logic;

import com.nfssoundtrack.newapproach.audio.GuildMusicManager;
import com.nfssoundtrack.newapproach.model.Song;
import com.nfssoundtrack.newapproach.others.MiscHelper;
import com.nfssoundtrack.newapproach.others.MissingPropertyException;
import com.nfssoundtrack.newapproach.others.Resources;
import com.nfssoundtrack.newapproach.others.SongHelper;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MessageHandler {

    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());

    static List<Song> handleFindCommand(MainTest mainTest, GuildMessageReceivedEvent event) {
        List<Song> songsToReturn = mainTest.songsFromFile;
        String messageRawContent = event.getMessage().getContentRaw();
        logger.log(Level.INFO, "Event message: " + messageRawContent);
        String[] command = messageRawContent.split("-");
        if (command.length < 2) {
            event.getChannel().sendMessage("Missing parameters in query, see example:\n" +
                    "~find [-title:\"Born Too Slow\"] [-band:\"The Crystal Method\"] [-game:\"Need For Speed Underground\"]").queue();
        } else {
            for (String parameter : command) {
                parameter = parameter.trim();
                if (parameter.startsWith("title")) {
                    String[] titleKey = parameter.split(":");
                    String filterValue = titleKey[1].replace("\"", "").toLowerCase();
                    songsToReturn = SongHelper.findSongsByTitle(filterValue, songsToReturn);
                    logger.log(Level.INFO, "Size of list after title filter: " + songsToReturn.size());
                }
                if (parameter.startsWith("band")) {
                    String[] bandKey = parameter.split(":");
                    String filterValue = bandKey[1].replace("\"", "").toLowerCase();
                    songsToReturn = SongHelper.findSongsByBand(filterValue, songsToReturn);
                    logger.log(Level.INFO, "Size of list after band filter: " + songsToReturn.size());
                }
                if (parameter.startsWith("game")) {
                    String[] gameKey = parameter.split(":");
                    String filterValue = gameKey[1].replace("\"", "").toLowerCase();
                    songsToReturn = SongHelper.findSongsByGame(filterValue, songsToReturn);
                    logger.log(Level.INFO, "Size of list after game filter: " + songsToReturn.size());
                }
            }
            logger.log(Level.INFO, "Final list size: " + songsToReturn.size());
            if (songsToReturn.size() == 0) {
                event.getChannel().sendMessage("No songs found for this query.").queue();
            } else {
                logger.log(Level.INFO, "Songs found: ");
                songsToReturn.forEach(song -> logger.log(Level.INFO, song.toRadioString()));
                int queueLimit = Integer.parseInt(MiscHelper.propertyValues.getProperty("queue.limit", "10"));
                if (songsToReturn.size() > queueLimit) {
                    event.getChannel().sendMessage("Found more than 10 songs matching criteria. First 10 songs are: ").queue();
                    songsToReturn = songsToReturn.stream().limit(queueLimit).collect(Collectors.toList());
                } else
                    event.getChannel().sendMessage("Found " + songsToReturn.size() + " songs matching criteria: ").queue();
                for (Song song : songsToReturn) {
                    event.getChannel().sendMessage(song.toRadioString()).queue();
                }
            }
        }
        return songsToReturn;
    }

    static void handleFindAndPlayFirst(MainTest mainTest, GuildMessageReceivedEvent event) {
        List<Song> filteredSongs = handleFindCommand(mainTest, event);
        if (!filteredSongs.isEmpty()) {
            Song firstSong = filteredSongs.stream().findFirst().get();
            event.getChannel().sendMessage("Song that will be played as first: " + firstSong.toRadioString()).queue();
            handlePlayLink(mainTest, event, Resources.YOUTUBE_LINK + firstSong.getSrc_id());
        }
    }

    static void handleFindAndPlayAll(MainTest mainTest, GuildMessageReceivedEvent event) {
        List<Song> filteredSongs = handleFindCommand(mainTest, event);
        handleFindAndPlayFirst(mainTest, event);
        int queueLimit = Integer.parseInt(MiscHelper.propertyValues.getProperty("queue.limit", "10"));
        filteredSongs = filteredSongs.stream().skip(1).limit(queueLimit).collect(Collectors.toList());
        for (Song song : filteredSongs) {
//            event.getChannel().sendMessage("Song added to queue: " + song.toRadioString()).queue();
            handlePlayLink(mainTest, event, Resources.YOUTUBE_LINK + song.getSrc_id());
        }
    }

    static void handleSkipCommand(MainTest mainTest, GuildMessageReceivedEvent event) {
        GuildMusicManager musicManager = mainTest.getGuildAudioPlayer(event.getChannel().getGuild());
        if (!musicManager.scheduler.getQueue().isEmpty()) {
            event.getChannel().sendMessage("Skipped to next track").queue();
        } else {
            event.getChannel().sendMessage("Queue already empty").queue();
            if (MainTest.isRadioModeEnabled) {
                event.getChannel().sendMessage("Radio mode enabled, getting new song").queue();
            }
        }
        musicManager.scheduler.nextTrack();
    }

    static void handleHelpCommand(GuildMessageReceivedEvent event) {
        event.getAuthor().openPrivateChannel().queue((privateChannel -> privateChannel.sendMessage(Resources.HELP_COMMAND_TEXT).queue()));
    }

    static void handleRandomSongCommand(MainTest mainTest, GuildMessageReceivedEvent event) {
        Song song = SongHelper.getRandomSong(mainTest.filteredSongs);
        if (!Boolean.parseBoolean(MiscHelper.propertyValues.getProperty("disable.next.song.message", "false"))) {
            event.getChannel().sendMessage("Next song will be: " + song.toRadioString()).queue();
        }
        mainTest.loadAndPlay(event.getChannel(), song.getSrc_id());
    }

    static void handleNowCommand(MainTest mainTest, GuildMessageReceivedEvent event) {
        AudioTrack track = mainTest.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack();
        if (track == null) {
            event.getChannel().sendMessage("Nothing played currently").queue();
        } else {
            Song dbSong = SongHelper.findSongBySrcId(track.getIdentifier(), mainTest.filteredSongs);
            if (dbSong != null) {
                event.getChannel().sendMessage("Song now is: " + dbSong.toRadioString()).queue();
            } else {
                event.getChannel().sendMessage("Song now is: " + track.getInfo().title
                        + " : " + track.getInfo().uri).queue();
            }
        }
    }

    static void handleClearCommand(MainTest mainTest, GuildMessageReceivedEvent event) {
        BlockingQueue<AudioTrack> queue = mainTest.getGuildAudioPlayer(event.getGuild()).scheduler.getQueue();
        queue.clear();
        event.getChannel().sendMessage("Queue cleared").queue();
    }


    static void handlePlayCommand(MainTest mainTest, GuildMessageReceivedEvent event) {
        String messageRawContent = event.getMessage().getContentRaw();
        logger.log(Level.INFO, "Event message: " + messageRawContent);
        String[] command = messageRawContent.split("-");
        if (command.length < 2) {
            event.getChannel().sendMessage("Missing parameters in command, see examples:\n" +
                    "~play [-id: 2135]\n" + "~play [-youtube: https://www.youtube.com/watch?v=<YT_ID>]").queue();
        } else {
            for (String parameter : command) {
                parameter = parameter.trim();
                if (parameter.startsWith("youtube")) {
                    //handle two formats:
                    //https://youtu.be/CEPX-k88xWA
                    //https://www.youtube.com/watch?v=CEPX-k88xWA
                    int normalLinkIndex = messageRawContent.indexOf(Resources.YOUTUBE_LINK);
                    if (normalLinkIndex != -1) {
                        String ytLink = messageRawContent.substring(normalLinkIndex + Resources.YOUTUBE_LINK.length())
                                .replaceAll("\"", "").trim();
                        handlePlayLink(mainTest, event, Resources.YOUTUBE_LINK + ytLink);
                    } else {
                        int shortLinkIndex = messageRawContent.indexOf(Resources.SHORT_YOUTUBE_LINK);
                        if (shortLinkIndex != -1) {
                            String ytLink = messageRawContent.substring(shortLinkIndex + Resources.SHORT_YOUTUBE_LINK.length())
                                    .replaceAll("\"", "").trim();
                            handlePlayLink(mainTest, event, Resources.SHORT_YOUTUBE_LINK + ytLink);
                        }
                    }
                }
                if (parameter.startsWith("id")) {
                    String[] titleKey = parameter.split(":");
                    String filterValue = titleKey[1].replace("\"", "");
                    Integer id = Integer.valueOf(filterValue);
                    Song foundSong = SongHelper.findSongById(id, mainTest.songsFromFile);
                    if (foundSong != null) {
                        event.getChannel().sendMessage("Song that will be played: " + foundSong.toRadioString()).queue();
                        handlePlayLink(mainTest, event, Resources.YOUTUBE_LINK + foundSong.getSrc_id());
                    }
                }
            }
        }
    }

    static void handlePlayLink(MainTest mainTest, GuildMessageReceivedEvent event, String ytLink) {
        mainTest.loadAndPlay(event.getChannel(), ytLink);
    }

    static void handleSetVolume(MainTest mainTest, GuildMessageReceivedEvent event) {
        String messageRawContent = event.getMessage().getContentRaw();
        logger.log(Level.INFO, "Event message: " + messageRawContent);
        String[] command = messageRawContent.split(" ");
        if (command.length != 2) {
            event.getChannel().sendMessage("Invalid input for command, see example:\n" +
                    "~setVolume 50 - sets bot volume to 50, possible values are between 0 and 100").queue();
            //write some info about wrong input
        } else {
            int volume = Integer.parseInt(command[1]);
            if (volume >= 0 && volume <= 100) {
                if (MiscHelper.getVersion() <= 8) {
                    event.getChannel().sendMessage("Detected Java version 8 or lower, you cannot set volume" +
                            ", please consider using Java 12 to enable this feature").queue();
                } else {
                    mainTest.getGuildAudioPlayer(event.getGuild()).player.setVolume(volume);
                    event.getChannel().sendMessage("Volume set to: " + volume).queue();
                }
            }
        }
    }

    static void handleReloadProperties(MainTest mainTest, GuildMessageReceivedEvent event) {
        try {
            MiscHelper.loadProperties();
            mainTest.filteredSongs = SongHelper.filterSongs(mainTest.songsFromFile);
            event.getChannel().sendMessage("Re-filtered songs by reloading values from default.properties").queue();
        } catch (MissingPropertyException | IOException propertyException) {
            logger.log(Level.WARNING, "Exception when reloading properties: " + propertyException.getMessage());
            propertyException.printStackTrace();
        }
    }

}
