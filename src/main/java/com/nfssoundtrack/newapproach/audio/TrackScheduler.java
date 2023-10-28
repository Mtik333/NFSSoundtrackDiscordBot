package com.nfssoundtrack.newapproach.audio;

import com.nfssoundtrack.newapproach.logic.MainTest;
import com.nfssoundtrack.newapproach.others.MiscHelper;
import com.nfssoundtrack.newapproach.others.Resources;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private final TextChannel textChannel;
    private final MainTest mainTest;
    private static final Logger logger = Logger.getLogger(TrackScheduler.class.getName());

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player, TextChannel textChannel, MainTest mainTest) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.textChannel = textChannel;
        this.mainTest = mainTest;
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        logger.log(Level.INFO, "Entering queue: parameters: track: " + track);
        if (!Boolean.parseBoolean(MiscHelper.propertyValues.getProperty("disable.song.info.message", "false"))) {
            textChannel.sendMessage("Track info: " + this.getTrackInfoPrettyToString(track)).queue();
        }
        if (track.getDuration() > Long.parseLong(MiscHelper.propertyValues
                .getProperty("song.max.length", "900000"))) {
            logger.log(Level.WARNING, "Song length exceeds set (or default) length limit");
            textChannel.sendMessage("Skipping song because it's too long").queue();
            textChannel.sendMessage(Resources.RANDOM_COMMAND).queue();
        } else if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
//        player.playTrack(queue.poll());
        player.startTrack(queue.poll(), false);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        logger.log(Level.INFO, "Entering onTrackStart: parameters: player: " + player + ", track: " + track);
        mainTest.setActivityInfo(textChannel.getGuild(), track);
        super.onTrackStart(player, track);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        logger.log(Level.INFO, "Entering onTrackEnd: parameters: player: " + player + ", track: " + track
                + ", endReason: " + endReason);
        if (endReason.mayStartNext) {
            nextTrack();
        }
        if (!textChannel.getJDA().getStatus().equals(JDA.Status.SHUTDOWN)) {
            if (!Boolean.parseBoolean(MiscHelper.propertyValues.getProperty("disable.song.ends.message", "false"))) {
                textChannel.sendMessage("Why song ends? " + endReason.toString()).queue();
            }
            if (this.queue.isEmpty()) {
                logger.log(Level.INFO, "Queue is empty");
                // ping website to avoid bot becoming idle
                MiscHelper.pingUrl(Resources.HEROKU_DOMAIN);
                if (MainTest.isRadioModeEnabled) {
                    textChannel.sendMessage("Queue is empty, getting new song").queue();
                    textChannel.sendMessage(Resources.RANDOM_COMMAND).queue();
                }
            }
        }
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    private String getTrackInfoPrettyToString(AudioTrack track) {
        logger.log(Level.INFO, "Entering getTrackInfoPrettyToString: parameters: track: " + track);
        StringBuilder stringBuilder = new StringBuilder();
        AudioTrackInfo info = track.getInfo();
        stringBuilder.append(info.title).append(";").append(info.length)
                .append(";").append(info.identifier);
        String finalInfo = stringBuilder.toString();
        logger.log(Level.INFO, "Pretty track info: " + finalInfo);
        return finalInfo;
    }
}
