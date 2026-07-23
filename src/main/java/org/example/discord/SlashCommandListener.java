package org.example.discord;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.clients.*;
import dev.lavalink.youtube.clients.skeleton.Client;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.thread.member.ThreadMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.AudioManager;
import org.example.audio.ServerMusicManager;
import org.example.audio.spotify.SpotifyService;
import org.example.audio.spotify.SpotifyTrack;
import org.example.audio.youtube.YoutubeService;
import org.example.telegram.TelegramBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SlashCommandListener extends ListenerAdapter {
    private final SlashCommand slashCommand;
    private final AudioPlayerManager playerManager;
    private final SpotifyService spotifyService;
    private final YoutubeService youtubeService;
    private final Map<Long, ServerMusicManager> musicManagers;
    private final Map<String, ServerMusicManager> pendingPins;

    public SlashCommandListener(SlashCommand slashCommand, SpotifyService spotifyService, YoutubeService youtubeService, Map<String, ServerMusicManager> pendingPins) {
        this.slashCommand = slashCommand;
        this.musicManagers = new ConcurrentHashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        this.spotifyService = spotifyService;
        this.youtubeService = youtubeService;
        this.pendingPins = pendingPins;

        dev.lavalink.youtube.YoutubeAudioSourceManager ytSourceManager = new dev.lavalink.youtube.YoutubeAudioSourceManager(
                true,
                new Client[] {
                        new MusicWithThumbnail(),
                        new AndroidVrWithThumbnail(),
                        new WebWithThumbnail(),
                        new WebEmbeddedWithThumbnail()
                }
        );

        this.playerManager.registerSourceManager(ytSourceManager);

        AudioSourceManagers.registerRemoteSources(
                this.playerManager,
                com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class
        );

        AudioSourceManagers.registerLocalSource(this.playerManager);
    }


    @Override
    public void onGuildReady(GuildReadyEvent event) {
        event.getGuild().updateCommands().addCommands(
                Commands.slash("say", "Makes the bot say what you tell it to")
                        .addOption(OptionType.STRING, "content", "What the bot should say", true),
                Commands.slash("currency", "Get the current exchange rate for USD"),
                Commands.slash("cat", "Get a beautiful image of a cat"),
                Commands.slash("best_cat", "Get an image of best breed of cats"),
                Commands.slash("weather", "Get weather information for a specific city")
                        .addOption(OptionType.STRING, "city", "City name", true),
                Commands.slash("forecast", "Get forecast information for a specific city")
                        .addOption(OptionType.STRING, "city", "City name", true),
                Commands.slash("forecast_detailed", "Get detailed forecast information for a specific city")
                        .addOption(OptionType.STRING, "city", "City name", true),
                Commands.slash("play", "Play a song from YouTube")
                        .addOption(OptionType.STRING, "name_or_url", "Name or URL (YouTube, Spotify) of the song", true),
                Commands.slash("skip", "Skip a song"),
                Commands.slash("stop", "Stop the song"),
                Commands.slash("pause", "Pause/Play the song"),
                Commands.slash("broadcast_tg", "Broadcast the song from Telegram Bot")
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "say" -> {
                String content = event.getOption("content", OptionMapping::getAsString);
                event.reply(content).queue();
            }
            case "currency" -> {
                event.deferReply().queue();
                String[] currencies = {"USD", "EUR"};
                EmbedBuilder result = slashCommand.getCurrencies(currencies);
                event.getHook().sendMessageEmbeds(result.build()).queue();
            }
            case "cat" -> {
                event.deferReply().queue();
                EmbedBuilder embed = slashCommand.getCat("");
                if (embed == null) {
                    event.getHook().sendMessage("а гдє").queue();
                }

                event.getHook().sendMessageEmbeds(embed.build()).queue();
            }
            case "best_cat" -> {
                event.deferReply().queue();
                EmbedBuilder embed = slashCommand.getCat("siam");
                if (embed == null) {
                    event.getHook().sendMessage("майкл нє в настроєніі").queue();
                    return;
                }

                event.getHook().sendMessageEmbeds(embed.build()).queue();
            }
            case "weather" -> {
                event.deferReply().queue();
                String city = event.getOption("city", OptionMapping::getAsString);
                EmbedBuilder embed = slashCommand.getWeather(city);
                if (embed == null) {
                    event.getHook().sendMessage("єта шо за мухасранск - " + city).queue();
                    return;
                }
                event.getHook().sendMessageEmbeds(embed.build()).queue();
            }
            case "forecast" -> {
                event.deferReply().queue();
                String city = event.getOption("city", OptionMapping::getAsString);
                EmbedBuilder embed = slashCommand.getForecast(city, false);
                if (embed == null) {
                    event.getHook().sendMessage("єта шо за мухасранск - " + city).queue();
                    return;
                }
                event.getHook().sendMessageEmbeds(embed.build()).queue();
            }
            case "forecast_detailed" -> {
                event.deferReply().queue();
                String city = event.getOption("city", OptionMapping::getAsString);
                EmbedBuilder embed = slashCommand.getForecast(city, true);
                if (embed == null) {
                    event.getHook().sendMessage("єта шо за мухасранск - " + city).queue();
                    return;
                }
                event.getHook().sendMessageEmbeds(embed.build()).queue();
            }
            case "play" -> {
                event.deferReply().queue();
                String url = event.getOption("name_or_url", OptionMapping::getAsString);

                GuildVoiceState voiceState = event.getMember().getVoiceState();

                if (voiceState == null || !voiceState.inAudioChannel()) {
                    event.getHook().sendMessage("чого нє в каналє?").queue();
                    return;
                }

                AudioChannel userVoiceChannel = voiceState.getChannel();

                AudioManager audioManager = event.getGuild().getAudioManager();
                audioManager.openAudioConnection(userVoiceChannel);

                ServerMusicManager musicManager = getOrCreateServerMusicManager(event.getGuild());
                musicManager.lastCommandChannel = event.getChannel();

                SpotifyTrack spotifyTrack = null;
                boolean isSearch = false;

                if (spotifyService.isValidTrackUrl(url)) {
                    try {
                        spotifyTrack = spotifyService.getSpotifyTrack(url);
                        url = "ytsearch:" + spotifyTrack.getArtist() + " - " + spotifyTrack.getTitle();
                    } catch (Exception e) {
                        event.getHook().sendMessage("❌ не вдалося прочитати спатіфай трєк: " + e.getMessage()).queue();
                        return;
                    }
                } else if (!youtubeService.isValidTrackUrl(url)) {
                    url = "ytsearch:" + url;
                    isSearch = true;
                }

                String finalUrl = url;
                SpotifyTrack finalSpotifyTrack = spotifyTrack;
                playerManager.loadItemOrdered(musicManager, url, new AudioLoadResultHandlerImpl(event, null, musicManager, finalUrl, finalSpotifyTrack, isSearch));
            }
            case "skip" -> {
                ServerMusicManager mgr = getOrCreateServerMusicManager(event.getGuild());
                mgr.scheduler.nextTrack();
                event.reply(":track_next: трєк пропущєн").queue();
            }
            case "stop" -> {
                ServerMusicManager mgr = getOrCreateServerMusicManager(event.getGuild());
                mgr.scheduler.queue.clear();
                mgr.player.stopTrack();
                event.reply(":stop_button: штоп").queue();
            }
            case "pause" -> {
                ServerMusicManager mgr = getOrCreateServerMusicManager(event.getGuild());
                boolean paused = !mgr.player.isPaused();
                mgr.player.setPaused(paused);
                event.reply(paused ? ":pause_button: бауза" : ":arrow_forward: паєхалі").queue();
            }
            case "broadcast_tg" -> {
                event.deferReply(true).queue();
                Dotenv dotenv = Dotenv.load();

                GuildVoiceState voiceState = event.getMember().getVoiceState();

                if (voiceState == null || !voiceState.inAudioChannel()) {
                    event.getHook().sendMessage("чого нє в каналє?").queue();
                    return;
                }

                AudioChannel userVoiceChannel = voiceState.getChannel();
                AudioManager audioManager = event.getGuild().getAudioManager();
                audioManager.openAudioConnection(userVoiceChannel);

                ServerMusicManager musicManager = getOrCreateServerMusicManager(event.getGuild());
                musicManager.lastCommandChannel = event.getChannel();
                String pin = String.format("%04d", (int)(Math.random() * 10000));
                pendingPins.put(pin, musicManager);

                event.getHook().sendMessage("ждьом тваєго сігнала с тг - **" + dotenv.get("TELEGRAM_BOT_NAME") + "**\nввєді в тг: **/pin " + pin + "**").queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();

        if (buttonId.startsWith("play_")) {
            String trackId = buttonId.replace("play_", "");

            event.deferReply().queue();
            event.getMessage().delete().queue();

            String trackUrl = "https://www.youtube.com/watch?v=" + trackId;

            ServerMusicManager musicManager = getOrCreateServerMusicManager(event.getGuild());
            musicManager.lastCommandChannel = event.getChannel();

            playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandlerImpl(null, event, musicManager, trackUrl, null, false));
        }
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        Guild guild = event.getGuild();
        Member selfMember = guild.getSelfMember();
        GuildVoiceState selfVoiceState = selfMember.getVoiceState();

        if (selfVoiceState == null || !selfVoiceState.inAudioChannel()) return;

        AudioChannel botChannel = selfVoiceState.getChannel();

        if (botChannel.equals(event.getChannelLeft()) || botChannel.equals(event.getChannelJoined())) {

            long humansInChannel = botChannel.getMembers().stream()
                    .filter(member -> !member.getUser().isBot())
                    .count();

            if (humansInChannel == 0) {

                long guildId = Long.parseLong(guild.getId());
                ServerMusicManager mgr = musicManagers.get(guildId);
                if (mgr != null) {
                    mgr.scheduler.queue.clear();
                    mgr.player.stopTrack();

                    if (mgr.lastCommandChannel != null) {
                        mgr.lastCommandChannel.sendMessage("👋 та пашлі ви").queue();
                    }
                }
                guild.getAudioManager().closeAudioConnection();
            }
        }
    }

    private synchronized ServerMusicManager getOrCreateServerMusicManager(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        ServerMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new ServerMusicManager(playerManager, guild);
            musicManagers.put(guildId, musicManager);
            guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        }

        return musicManager;
    }
}
