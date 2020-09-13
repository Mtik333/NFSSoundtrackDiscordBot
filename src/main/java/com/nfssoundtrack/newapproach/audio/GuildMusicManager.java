package com.nfssoundtrack.newapproach.audio;

import com.nfssoundtrack.newapproach.logic.MainTest;
import com.nfssoundtrack.newapproach.others.MiscHelper;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildMusicManager {
    /**
     * Audio player for the guild.
     */
    public final AudioPlayer player;
    /**
     * Track scheduler for the player.
     */
    public final TrackScheduler scheduler;
    private static final Logger logger = Logger.getLogger(GuildMusicManager.class.getName());

    /**
     * Creates a player and a track scheduler.
     *
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildMusicManager(AudioPlayerManager manager, TextChannel songRequestChannel, MainTest mainTest) {
        logger.log(Level.INFO, "Entered constructor of GuildMusicManager, parameters: manager: " + manager
                + ", songRequestChannel: " + songRequestChannel + ", mainTest: " + mainTest);
        player = manager.createPlayer();
        if (MiscHelper.getVersion() <= 8) {
            logger.log(Level.WARNING, "Java 8 does not support setting sound for some reason");
            songRequestChannel.sendMessage("Detected Java version 8 or lower, you cannot set volume" +
                    ", please consider using Java 12 to enable this feature").queue();
        } else player.setVolume(Integer.parseInt(MiscHelper.propertyValues.getProperty("volume.start", "100")));
        logger.log(Level.INFO, "Initial volume level: " + player.getVolume());
        scheduler = new TrackScheduler(player, songRequestChannel, mainTest);
        player.addListener(scheduler);
    }

    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }
}