package com.nfssoundtrack.newapproach.audio;

import com.nfssoundtrack.newapproach.logic.MainTest;
import com.nfssoundtrack.newapproach.others.MiscHelper;
import com.nfssoundtrack.newapproach.others.Resources;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MyAudioLoadResultHandler implements AudioLoadResultHandler {
    private final MainTest main;
    private final TextChannel channel;
    private final GuildMusicManager musicManager;
    private final String trackUrl;
    private static final Logger logger = Logger.getLogger(MyAudioLoadResultHandler.class.getName());

    public MyAudioLoadResultHandler(MainTest main, TextChannel channel, GuildMusicManager musicManager, String trackUrl) {
        this.main = main;
        this.channel = channel;
        this.musicManager = musicManager;
        this.trackUrl = trackUrl;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        logger.log(Level.INFO, "Entering trackLoaded, parameters: track: " + track);
        main.play(channel.getGuild(), musicManager, track);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        AudioTrack firstTrack = playlist.getSelectedTrack();
        if (firstTrack == null) {
            firstTrack = playlist.getTracks().get(0);
        }
        channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();
        main.play(channel.getGuild(), musicManager, firstTrack);
    }

    @Override
    public void noMatches() {
        logger.log(Level.INFO, "Entering noMatches");
        channel.sendMessage("Nothing found by " + trackUrl).queue();
        if (MainTest.isRadioModeEnabled) {
            channel.sendMessage(Resources.RANDOM_COMMAND).queue();
        }

    }

    @Override
    public void loadFailed(FriendlyException exception) {
        logger.log(Level.INFO, "Entering loadFailed: parameters: exception: " + exception);
        String exceptionMessage = exception.getMessage();
        channel.sendMessage("Could not play: " + exceptionMessage).queue();
        if (exceptionMessage.contains("Loading information for a YouTube track failed")) {
            //Could not play: Loading information for a YouTube track failed (error 429 - make bot wait 30 seconds for example)
            logger.log(Level.WARNING, "Seems to be an error 429? " + exception.getCause());
            exception.printStackTrace();
            channel.sendMessage("Error 429 - too many requests, bot will sleep for a minute").queue();
            channel.getJDA().getPresence().setActivity(Activity.listening("Dear admin, YouTube blocked me"));
            String adminId = MiscHelper.propertyValues.getProperty("pingadmin.id");
            logger.log(Level.INFO, "Admin id from properties: " + adminId);
            if (adminId != null && !adminId.isEmpty()) {
                User adminUser = channel.getJDA().getUserById(adminId);
                logger.log(Level.INFO, "User is: " + adminUser);
                if (adminUser != null) {
                    adminUser.openPrivateChannel().queue((privateChannel
                            -> privateChannel.sendMessage("Hi! I got error 429.\n" + exceptionMessage).queue()));
                } else {
                    logger.log(Level.WARNING, "No admin id provided in proeprties file, cannot communicate problem with bot");
                }
            }
            try {
                Thread.sleep(6000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (MainTest.isRadioModeEnabled) {
            channel.sendMessage(Resources.RANDOM_COMMAND).queue();
        }
    }
}
