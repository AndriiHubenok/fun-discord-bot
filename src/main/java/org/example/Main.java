package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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

        ApiInteraction apiInteraction = new ApiInteraction();
        SlashCommand slashCommand = new SlashCommand(apiInteraction);
        JDA jda = JDABuilder.createLight(botToken, Collections.emptyList())
                .addEventListeners(new SlashCommandListener(slashCommand))
                .build();
    }
}