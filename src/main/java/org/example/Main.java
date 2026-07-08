package org.example;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.lavalink.youtube.clients.AndroidVrWithThumbnail;
import dev.lavalink.youtube.clients.MusicWithThumbnail;
import dev.lavalink.youtube.clients.WebEmbeddedWithThumbnail;
import dev.lavalink.youtube.clients.WebWithThumbnail;
import dev.lavalink.youtube.clients.skeleton.Client;
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
import org.example.audio.ServerMusicManager;
import org.example.audio.spotify.SpotifyService;
import org.example.discord.SlashCommand;
import org.example.discord.SlashCommandListener;
import org.example.telegram.TelegramBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class Main {
    Logger logger = Logger.getLogger(getClass().getName());

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        String botToken = dotenv.get("DISCORD_BOT_TOKEN");

        if (botToken == null || botToken.isEmpty()) {
            System.err.println("Error: Bot token is not set in the environment variables. Please set the TOKEN variable.");
            System.exit(1);
        }

        DaveFactory daveFactory = new NativeDaveFactory();
        LDJDADaveSessionFactory daveSessionFactory = new LDJDADaveSessionFactory(daveFactory);

        ApiInteraction apiInteraction = new ApiInteraction();
        SlashCommand slashCommand = new SlashCommand(apiInteraction);
        SpotifyService spotifyService = new SpotifyService();

        Cache<String, ServerMusicManager> pendingPinsCache = Caffeine.newBuilder()
                .expireAfterWrite(3, TimeUnit.MINUTES)
                .build();
        Map<String, ServerMusicManager> pendingPins = pendingPinsCache.asMap();

        Cache<Long, ServerMusicManager> activeTgSessionsCache = Caffeine.newBuilder()
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build();
        Map<Long, ServerMusicManager> activeTgSessions = activeTgSessionsCache.asMap();

        JDA jda = JDABuilder.createLight(botToken, Collections.emptyList())
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
                .enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(new SlashCommandListener(slashCommand, spotifyService, pendingPins))
                .setAudioModuleConfig(new AudioModuleConfig().withDaveSessionFactory(daveSessionFactory))
                .build();

        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        dev.lavalink.youtube.YoutubeAudioSourceManager ytSourceManager = new dev.lavalink.youtube.YoutubeAudioSourceManager(
                true,
                new Client[] {
                        new MusicWithThumbnail(),
                        new AndroidVrWithThumbnail(),
                        new WebWithThumbnail(),
                        new WebEmbeddedWithThumbnail()
                }
        );

        playerManager.registerSourceManager(ytSourceManager);

        AudioSourceManagers.registerRemoteSources(
                playerManager,
                com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class
        );

        AudioSourceManagers.registerLocalSource(playerManager);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramBot(
                    dotenv.get("TELEGRAM_BOT_TOKEN"),
                    playerManager,
                    pendingPins,
                    activeTgSessions
            ));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}