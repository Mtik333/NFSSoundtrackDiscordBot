/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.heroku;

import com.nfssoundtrack.newapproach.logic.MainTest;
import com.nfssoundtrack.newapproach.others.MiscHelper;
import com.nfssoundtrack.newapproach.others.MissingPropertyException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@SpringBootApplication
public class HerokuApplication {

    private JDA jda;
    private static final Logger logger = Logger.getLogger(HerokuApplication.class.getName());

    public static void main(String[] args) {
        logger.log(Level.INFO, "Starting Spring application");
        try {
            MiscHelper.loadProperties();
            MiscHelper.initializeSessionFactory();
        } catch (MissingPropertyException propertyException) {
            logger.log(Level.SEVERE, "Properties file has some errors: \n" + propertyException.getMessage());
            return;
        } catch (IOException ioException) {
            logger.log(Level.SEVERE, "Couldn't load 'default.properties' file: " + ioException.getMessage());
            return;
        }
        SpringApplication.run(HerokuApplication.class, args);
    }

    @SuppressWarnings("unused")
    @RequestMapping("/start")
    String start(Map<String, Object> model) throws LoginException, InterruptedException {
        logger.log(Level.INFO, "Entered 'start' endpoint, launching bot");
        if (MiscHelper.isBotStarted(jda)) {
            logger.log(Level.WARNING, "Bot already started and connected to voice channel");
            model.put("message", "Bot already started");
            jda.shutdown();
            return "error";
        } else {
            jda = JDABuilder.create(MiscHelper.propertyValues.getProperty("bot.token"),
                            EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES))
                    .addEventListeners(new MainTest())
                    .setActivity(Activity.playing("wake up, we have soundtracks to play"))
                    .build();
            jda.awaitReady();
        }
        return "start";
    }

    @SuppressWarnings({"unused", "SameReturnValue"})
    @RequestMapping("/stop")
    String stop() {
        logger.log(Level.INFO, "Entered 'stop' endpoint, stopping bot");
        if (jda != null) {
            jda.shutdown();
        }
        return "stop";
    }

    @SuppressWarnings({"unused", "SameReturnValue"})
    @RequestMapping("/radioon")
    String radioModeOn() {
        logger.log(Level.INFO, "Entered 'radioon' endpoint, enabling radio mode");
        if (jda != null) {
            MainTest.isRadioModeEnabled = true;
        }
        return "radioon";
    }

    @SuppressWarnings({"unused", "SameReturnValue"})
    @RequestMapping("/radiooff")
    String radioModeOff() {
        logger.log(Level.INFO, "Entered 'radiooff' endpoint, disabling radio mode");
        if (jda != null) {
            MainTest.isRadioModeEnabled = false;
        }
        return "radiooff";
    }

}
