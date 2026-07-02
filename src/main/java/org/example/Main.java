package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import moe.kyokobot.libdave.DaveFactory;
import moe.kyokobot.libdave.NativeDaveFactory;
import moe.kyokobot.libdave.jda.LDJDADaveSessionFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.example.api.ApiInteraction;

import java.util.Collections;
import java.util.logging.Logger;


public class Main {
    Logger logger = Logger.getLogger(getClass().getName());

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        String botToken = dotenv.get("TOKEN");

        if (botToken == null || botToken.isEmpty()) {
            System.err.println("Error: Bot token is not set in the environment variables. Please set the TOKEN variable.");
            System.exit(1);
        }

        DaveFactory daveFactory = new NativeDaveFactory();
        LDJDADaveSessionFactory daveSessionFactory = new LDJDADaveSessionFactory(daveFactory);

        ApiInteraction apiInteraction = new ApiInteraction();
        SlashCommand slashCommand = new SlashCommand(apiInteraction);
        JDA jda = JDABuilder.createLight(botToken, Collections.emptyList())
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
                .enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(new SlashCommandListener(slashCommand))
                .setAudioModuleConfig(new AudioModuleConfig().withDaveSessionFactory(daveSessionFactory))
                .build();
    }
}