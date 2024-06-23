package com.nfssoundtrack.newapproach.logic;

import com.nfssoundtrack.newapproach.audio.GuildMusicManager;
import com.nfssoundtrack.newapproach.audio.MyAudioLoadResultHandler;
import com.nfssoundtrack.newapproach.model2.Games;
import com.nfssoundtrack.newapproach.model2.Series;
import com.nfssoundtrack.newapproach.model2.Songs;
import com.nfssoundtrack.newapproach.others.MiscHelper;
import com.nfssoundtrack.newapproach.others.Queries;
import com.nfssoundtrack.newapproach.others.Resources;
import com.nfssoundtrack.newapproach.others.SongHelper;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainTest extends ListenerAdapter {

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;
    private TextChannel radioChannel;
    List<Songs> filteredSongs;
    public static boolean isRadioModeEnabled = true;
    private static final Logger logger = Logger.getLogger(MainTest.class.getName());

    public MainTest() {
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        dev.lavalink.youtube.YoutubeAudioSourceManager youtubeAudioSourceManager = new dev.lavalink.youtube.YoutubeAudioSourceManager(true);
        this.playerManager.setFrameBufferDuration(2000);
        this.playerManager.getConfiguration().setResamplingQuality
                (AudioConfiguration.ResamplingQuality.valueOf(MiscHelper.propertyValues.getProperty("quality.level", "HIGH")));
        playerManager.registerSourceManager(youtubeAudioSourceManager);
        AudioSourceManagers.registerRemoteSources(playerManager);
//        AudioSourceManagers.registerLocalSource(playerManager);
        this.filteredSongs = SongHelper.filterSongs(new ArrayList<>(), null, null, null);
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        logger.log(Level.INFO, "MainTest.onReady: event: " + event);
        logger.log(Level.INFO, "Is radio mode enabled? " + isRadioModeEnabled);
        radioChannel = event.getJDA().getTextChannelById(MiscHelper.propertyValues.getProperty("textchannel.id"));
        logger.log(Level.INFO, "Radio channel: " + radioChannel);
        if (radioChannel != null) {
            radioChannel.sendMessage(Resources.BOT_STARTED_MESSAGE).queue();
            if (isRadioModeEnabled) {
                radioChannel.sendMessage(Resources.RANDOM_COMMAND).queue();
            } else {
                radioChannel.getJDA().getPresence().setActivity(Activity.listening("Waiting for someone to listen to music with me"));
            }
        }
        super.onReady(event);
    }

    synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        logger.log(Level.INFO, "getGuildAudioPlayer: guild" + guild + "; id: " + guildId);
        GuildMusicManager musicManager = musicManagers.get(guildId);
        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager, radioChannel, this);
            musicManagers.put(guildId, musicManager);
        }
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        return musicManager;
    }


    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        logger.log(Level.INFO, "onGuildMessageReceived: event: " + event);
        String textChannelId = MiscHelper.propertyValues.getProperty("textchannel.id");
        String[] command = event.getMessage().getContentRaw().split(" ", 2);
        boolean userAllowed = true;
        if (event.getAuthor().getId().equals(MiscHelper.propertyValues.getProperty("pingadmin.id"))
                || event.getAuthor().getId().equals(MiscHelper.propertyValues.getProperty("bot.id"))) {
            logger.log(Level.INFO, "allgood");
        } else {
            String usersAllowed = MiscHelper.propertyValues.getProperty("allowedusers.id");
            if (usersAllowed != null && !usersAllowed.isEmpty()) {
                List<String> allowedIds = Arrays.stream(usersAllowed.split(",")).toList();
                if (allowedIds.contains(event.getAuthor().getId())) {
                    logger.log(Level.INFO, "allgood");
                } else {
                    if (event.getChannel().getId().contentEquals(textChannelId)) {
                        event.getChannel().sendMessage("User " + event.getAuthor().getName()
                                + " is not allowed to do " + "anything with this radio bot").queue();
                        event.getAuthor().openPrivateChannel().queue((privateChannel ->
                                privateChannel.sendMessage("You're not allowed to change the radio, " +
                                        "ask admin to get such permissions").queue()));
                        userAllowed = false;
                    }
                }
            } else {
                if (event.getChannel().getId().contentEquals(textChannelId)) {
                    event.getChannel().sendMessage("User " + event.getAuthor().getName()
                            + " is not allowed to do " + "anything with this radio bot").queue();
                    event.getAuthor().openPrivateChannel().queue((privateChannel ->
                            privateChannel.sendMessage("You're not allowed to change the radio, " +
                                    "ask admin to get such permissions").queue()));
                    userAllowed = false;
                }
            }
        }
        logger.log(Level.INFO, "Command sent: " + Arrays.toString(command));
        if (event.getChannel().getId().contentEquals(MiscHelper.propertyValues.getProperty("textchannel.id"))) {
            //find song with some constraints
            if (Resources.FIND_COMMAND.equals(command[0]) && userAllowed) {
                MessageHandler.handleFindCommand(this, event);
            } else if (Resources.FIND_PLAY_FIRST_COMMAND.equals(command[0]) && userAllowed) {
                MessageHandler.handleFindAndPlayFirst(this, event);
            } else if (Resources.FIND_PLAY_ALL_COMMAND.equals(command[0]) && userAllowed) {
                MessageHandler.handleFindAndPlayAll(this, event);
            }
            //just play whatever someone wants
            else if (Resources.PLAY_COMMAND.equals(command[0]) && userAllowed) {
                MessageHandler.handlePlayCommand(this, event);
            }
            //skip current song but maybe introduce some permissions to do this?
            else if (Resources.SKIP_COMMAND.equals(command[0]) && userAllowed) {
                MessageHandler.handleSkipCommand(this, event);
            }
            //add random song from database to queue?
            else if (Resources.RANDOM_COMMAND.equals(command[0]) && userAllowed) {
                MessageHandler.handleRandomSongCommand(this, event);
            }
            //show info about currently played song?
            else if (Resources.NOW_COMMAND.equals(command[0])) {
                MessageHandler.handleNowCommand(this, event);
            } else if (Resources.HELP_COMMAND.equals(command[0])) {
                MessageHandler.handleHelpCommand(event);
            } else if (Resources.QUEUE_COMMAND.equals(command[0])) {
                BlockingQueue<AudioTrack> queue = getGuildAudioPlayer(event.getGuild()).scheduler.getQueue();
                logger.log(Level.INFO, "Queue is: " + queue);
                event.getChannel().sendMessage("Size of queue: " + queue.size()).queue();
                int i = 1;
                for (Iterator<AudioTrack> audio = queue.iterator(); audio.hasNext(); i++) {
                    AudioTrack track = audio.next();
                    Songs dbSong = Queries.getSongsBySrcId(track.getIdentifier());
                    if (dbSong != null) {
                        event.getChannel().sendMessage("Song in queue on position " + i + ": " + dbSong.toRadioString()).queue();
                    } else {
                        event.getChannel().sendMessage("Song in queue on position " + i + ": " + track).queue();
                    }
                }
            } else if (Resources.CLEAR_COMMAND.equals(command[0]) && userAllowed) {
                MessageHandler.handleClearCommand(this, event);
            } else if (Resources.SET_VOLUME_COMMAND.equals(command[0]) && userAllowed) {
                MessageHandler.handleSetVolume(this, event);
            } else if (Resources.SET_SERIES_FILTER_COMMAND.equals(command[0]) && userAllowed) {
                MessageHandler.handleFilter(this, event, Series.class);
            } else if (Resources.SET_GAMES_FILTER_COMMAND.equals(command[0]) && userAllowed) {
                MessageHandler.handleFilter(this, event, Games.class);
            } else if (Resources.SET_SONGS_FILTER_COMMAND.equals(command[0]) && userAllowed) {
                MessageHandler.handleFilter(this, event, Songs.class);
            } else if (Resources.RESET_FILTER_COMMAND.equals(command[0]) && userAllowed) {
                MessageHandler.handleResetFilter(this, event);
            } else if ("~reloadProperties".equals(command[0]) && userAllowed) {
                MessageHandler.handleReloadProperties(this, event);
            }
        }
        super.onGuildMessageReceived(event);
    }

    void loadAndPlay(final TextChannel channel, final String trackUrl) {
        logger.log(Level.INFO, "loadAndPlay: channel: " + channel + ", trackUrl: " + trackUrl);
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        playerManager.loadItemOrdered(musicManager, trackUrl, new MyAudioLoadResultHandler(this, channel, musicManager, trackUrl));
    }

    public void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        logger.log(Level.INFO, "play: guild: " + guild + ", musicManager: " + musicManager + ", track: " + track);
        connectToFirstVoiceChannel(guild.getAudioManager());
        musicManager.scheduler.queue(track);
    }

    public void setActivityInfo(Guild guild, AudioTrack track) {
        logger.log(Level.INFO, "setActivityInfo: guild: " + guild + ", track: " + track);
        Songs dbSong = Queries.getSongsBySrcId(track.getIdentifier());
        logger.log(Level.INFO, "Current activity: " + guild.getJDA().getPresence().getActivity());
        if (dbSong == null) {
            guild.getJDA().getPresence().setActivity(Activity.listening(track.getInfo().title));
        } else {
            guild.getJDA().getPresence().setActivity(Activity.listening(dbSong.toStatusString()));
        }
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
        logger.log(Level.INFO, "connectToFirstVoiceChannel: " + audioManager);
        if (!audioManager.isConnected()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                if (voiceChannel.getId().contentEquals(MiscHelper.propertyValues.getProperty("voicechannel.id"))) {
                    audioManager.openAudioConnection(voiceChannel);
                    break;
                }
            }
        }
    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        if (event.getChannelLeft().getId().contentEquals(MiscHelper.propertyValues.getProperty("voicechannel.id"))) {
            List<Member> members = event.getChannelLeft().getMembers();
            logger.log(Level.INFO, "onGuildVoiceLeave: event: " + event + ", Users: " + members);
            boolean isOnlyBotOnChannel = members.stream().allMatch(member -> member.getUser().isBot());
            boolean shouldStopOnEmptyChannel = Boolean.parseBoolean(MiscHelper.propertyValues.getProperty("stop.on.empty.channel"));
            logger.log(Level.INFO, "isOnlyBotOnChannel: " + isOnlyBotOnChannel + ", shouldStopOnEmptyChannel: " + shouldStopOnEmptyChannel);
            if (isOnlyBotOnChannel && shouldStopOnEmptyChannel) {
                isRadioModeEnabled = false;
                radioChannel.sendMessage("Only bot remained on channel, stopped playing music").queue();
                radioChannel.getJDA().getPresence().setActivity(Activity.listening("Waiting for someone to listen to music with me"));
                radioChannel.sendMessage(Resources.CLEAR_COMMAND).queue();
                radioChannel.sendMessage(Resources.SKIP_COMMAND).queue();
            }
        }
        super.onGuildVoiceLeave(event);
    }

    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {
        if (event.getChannelJoined().getId().contentEquals((MiscHelper.propertyValues.getProperty("voicechannel.id")))) {
            List<Member> members = event.getChannelJoined().getMembers();
            StringBuilder membersOnChannel = new StringBuilder();
            logger.log(Level.INFO, "onGuildVoiceJoin: event: " + event + ", Users: " + members);
            for (Member member : members) {
                logger.log(Level.INFO, "Member: " + member.toString());
                membersOnChannel.append(member.getEffectiveName()).append("; ");
            }
            boolean isNotOnlyBotOnChannel = members.stream().anyMatch(member -> !member.getUser().isBot());
            long howManyRealUsers = members.stream().filter(member -> !member.getUser().isBot()).count();
            int queueSize = getGuildAudioPlayer(event.getGuild()).scheduler.getQueue().size();
            logger.log(Level.INFO, "isNotOnlyBotOnChannel: " + isNotOnlyBotOnChannel + ", howManyRealUsers: " + howManyRealUsers);
            if (isNotOnlyBotOnChannel) {
                isRadioModeEnabled = true;
                radioChannel.sendMessage("Who joined? " + event.getMember().getEffectiveName()).queue();
                radioChannel.sendMessage("Who is on channel? " + membersOnChannel).queue();
                if (howManyRealUsers >= 1 && queueSize == 0) {
                    radioChannel.sendMessage(Resources.RANDOM_COMMAND).queue();
                }
            }
        }
        super.onGuildVoiceJoin(event);
    }


}
