package com.nfssoundtrack.newapproach.others;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MiscHelper {

    public static Properties propertyValues;
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final Logger logger = Logger.getLogger(MiscHelper.class.getName());

    public static void loadProperties() throws MissingPropertyException, IOException {
        logger.log(Level.INFO, "Entering loadProperties");
        String runtimePath = MiscHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        logger.log(Level.INFO, "Path: " + runtimePath);
        String valuesPath;
        if (runtimePath.toLowerCase().contains(".jar")) {
            String testPath = "";
            if (isWindows()) {
                logger.log(Level.INFO, "Running on Windows system");
                //path always starts with file:/
                testPath = runtimePath.substring(6, runtimePath.indexOf(".jar")) + "default.properties";
                testPath = testPath.replace("NFSSoundtrack-Radio-0.7", "");
            } else if (isUnix()) {
                logger.log(Level.INFO, "Running on Unix system");
                testPath = runtimePath.substring(5, runtimePath.indexOf(".jar")) + "default.properties";
                testPath = testPath.replace("NFSSoundtrack-Radio-0.7", "");
            } else if (isMac()){
                logger.log(Level.INFO, "I don't know");
            }
            logger.log(Level.INFO, "Path to properties file: " + testPath);
            File valuesFile = new File(testPath);
            valuesPath = valuesFile.getPath();
            InputStream in = new FileInputStream(valuesPath);
            propertyValues = new Properties();
            propertyValues.load(in);
            logger.log(Level.INFO, "Properties loaded correctly (JAR-mode)");
        } else {
            valuesPath = MiscHelper.class.getResource("/bot/default.properties").getPath();
            InputStream in = new FileInputStream(valuesPath);
            propertyValues = new Properties();
            propertyValues.load(in);
            logger.log(Level.INFO, "Properties loaded correctly (non-JAR-mode)");
        }
        validateProperties();
    }

    private static void validateProperties() throws MissingPropertyException {
        logger.log(Level.INFO, "Entering validateProperties");
        String missingProperties = "";
        if (!propertyValues.containsKey("voicechannel.id") || propertyValues.getProperty("voicechannel.id").isEmpty()) {
            missingProperties = missingProperties + ("File 'default.properties' has missing value for property: voicechannel.id\n");
        }
        if (!propertyValues.containsKey("textchannel.id") || propertyValues.getProperty("textchannel.id").isEmpty()) {
            missingProperties = missingProperties + ("File 'default.properties' has missing value for property: textchannel.id\n");
        }
        if (!propertyValues.containsKey("bot.id") || propertyValues.getProperty("bot.id").isEmpty()) {
            missingProperties = missingProperties + ("File 'default.properties' has missing value for property: bot.id\n");
        }
        if (!propertyValues.containsKey("bot.token") || propertyValues.getProperty("bot.token").isEmpty()) {
            missingProperties = missingProperties + ("File 'default.properties' has missing value for property: bot.token\n");
        }
        logger.log(Level.INFO, "Missing properties: " + missingProperties);
        if (!missingProperties.isEmpty()) {
            throw new MissingPropertyException(missingProperties);
        }
    }

    public static boolean pingUrl(final String address) {
        logger.log(Level.INFO, "Pinging Heroku domain to keep radio alive");
        try {
            final URL url = new URL(address);
            final HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setConnectTimeout(1000 * 10); // mTimeout is in seconds
            urlConn.connect();
            if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                logger.log(Level.INFO, "Successfully pinged Heroku");
                return true;
            }
        } catch (final IOException ioException) {
            logger.log(Level.SEVERE, "Problem with pinging Heroku: " + ioException.getMessage());
            ioException.printStackTrace();
        }
        return false;
    }

    public static boolean isBotStarted(JDA jda) {
        logger.log(Level.INFO, "Entering isBotStarted: parameters: jda: " + jda);
        if (jda!=null){
            VoiceChannel voiceChannelFromProperties = jda
                    .getVoiceChannelById(propertyValues.getProperty("voicechannel.id"));
            if (voiceChannelFromProperties!=null){
                List<Member> voiceChannelMembers = voiceChannelFromProperties.getMembers();
                Optional<Member> botConnected = voiceChannelMembers.stream().filter(member ->
                        member.getId().contentEquals(propertyValues.getProperty("bot.id"))).findAny();
                logger.log(Level.INFO, "Is bot connected? " + botConnected.isPresent());
                return botConnected.isPresent();
            } else {
                logger.log(Level.INFO, "Voice channel not found");
                return false;
            }
        }
        else return false;
    }

    public static int getVersion() {
        String version = System.getProperty("java.version");
        logger.log(Level.INFO, "Entering getVersion, version from system property: " + version);
        if(version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if(dot != -1) { version = version.substring(0, dot); }
        }
        int digitVersion = Integer.parseInt(version);
        logger.log(Level.INFO, "Normalized version of Java: " + digitVersion);
        return Integer.parseInt(version);
    }

    public static boolean isWindows() {
        return (OS.contains("win"));
    }

    public static boolean isMac() {
        return (OS.contains("mac"));
    }

    public static boolean isUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0);
    }
}
