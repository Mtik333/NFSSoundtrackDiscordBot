package com.nfssoundtrack.newapproach.others;

public class Resources {

    public static final String HELP_COMMAND_TEXT = "List of commands:\n" +
            "~find [-title:\"Born Too Slow\"] [-band:\"The Crystal Method\"] [-game:\"Need For Speed Underground\"] - this will look for song with criteria that match provided title, band and/or game; command will provide NFSSoundtrack DB Info\n" +
            "~findAndPlayFirst [-title:\"Born Too Slow\"] [-band:\"The Crystal Method\"] [-game:\"Need For Speed Underground\"] - find song, if at least one found, play first\n" +
            "~findAndPlayAll [-title:\"Born Too Slow\"] [-band:\"The Crystal Method\"] [-game:\"Need For Speed Underground\"] - find song, if at least one found, add all to queue (max. 10 can be added for now)\n" +
            "~play [-id: 2135] - plays song with NFSSoundtrack id provided via find command\n" +
            "~play [-youtube: https://www.youtube.com/watch?v=YOUTUBE_ID] - plays song with YouTube id provided via find command\n" +
            "~skip - skips current song and gets next song in queue (permissions required)\n" +
            "~random - gets song from NFSSoundtrack DB and adds it to queue\n" +
            "~now - prints info about currently played song (with info from NFSSoundtrack when possible)\n" +
            "~queue - prints info about songs in queue, you can set queue maximum size in properties file\n" +
            "~clear - clears queue but currently played song keeps playing\n" +
            "~setVolume [0-100] - sets bot volume between 0 and 100, you can set default volume in properties file";
    public static final String YOUTUBE_LINK = "youtube.com/watch?v=";
    public static final String SHORT_YOUTUBE_LINK = "youtu.be/";
    public static final String HEROKU_DOMAIN = "https://nfssoundtrack-radio.herokuapp.com/";
    public static final String BOT_STARTED_MESSAGE = "NFSSoundtrack Radio Bot started";
    public static final String RANDOM_COMMAND = "~random";
    public static final String FIND_COMMAND = "~find";
    public static final String FIND_PLAY_FIRST_COMMAND = "~findAndPlayFirst";
    public static final String FIND_PLAY_ALL_COMMAND = "~findAndPlayAll";
    public static final String PLAY_COMMAND = "~play";
    public static final String SKIP_COMMAND = "~skip";
    public static final String NOW_COMMAND = "~now";
    public static final String HELP_COMMAND = "~help";
    public static final String QUEUE_COMMAND = "~queue";
    public static final String CLEAR_COMMAND = "~clear";
    public static final String SET_VOLUME_COMMAND = "~setVolume";
}
