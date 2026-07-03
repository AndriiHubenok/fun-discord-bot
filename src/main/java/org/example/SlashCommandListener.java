package org.example;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.clients.*;
import dev.lavalink.youtube.clients.skeleton.Client;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.AudioManager;
import org.example.audio.ServerMusicManager;
import org.example.audio.spotify.SpotifyService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SlashCommandListener extends ListenerAdapter {
    private final SlashCommand slashCommand;
    private final AudioPlayerManager playerManager;
    private final SpotifyService spotifyService;
    private final Map<Long, ServerMusicManager> musicManagers;

    public SlashCommandListener(SlashCommand slashCommand, SpotifyService spotifyService) {
        this.slashCommand = slashCommand;
        this.musicManagers = new ConcurrentHashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        this.spotifyService = spotifyService;

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
                        .addOption(OptionType.STRING, "url", "YouTube URL of the song", true),
                Commands.slash("stop", "Stop the song"),
                Commands.slash("pause", "Pause/Play the song"),
                Commands.slash("leave", "Makes the bot leave the server")
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
                String url = event.getOption("url", OptionMapping::getAsString);

                GuildVoiceState voiceState = event.getMember().getVoiceState();

                if (voiceState == null || !voiceState.inAudioChannel()) {
                    event.getHook().sendMessage("чого нє в каналє?").queue();
                    return;
                }

                AudioChannel userVoiceChannel = voiceState.getChannel();

                AudioManager audioManager = event.getGuild().getAudioManager();
                audioManager.openAudioConnection(userVoiceChannel);

                ServerMusicManager musicManager = getOrCreateServerMusicManager(event.getGuild());

                if (spotifyService.isSpotifyTrackUrl(url)) {
                    try {
                        url = spotifyService.convertTrackUrlToYoutubeSearch(url);
                    } catch (Exception e) {
                        event.getHook().sendMessage("❌ не вдалося прочитати спатіфай трєк: " + e.getMessage()).queue();
                        return;
                    }
                }

                playerManager.loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        musicManager.player.playTrack(track);
                        event.getHook().sendMessage("▶️ вмікаю: " + track.getInfo().title).queue();
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        AudioTrack firstTrack = playlist.getSelectedTrack();
                        if (firstTrack == null) {
                            firstTrack = playlist.getTracks().get(0);
                        }
                        musicManager.player.playTrack(firstTrack);
                        event.getHook().sendMessage("▶️ вмікаю трєк із плейліста: " + firstTrack.getInfo().title).queue();
                    }

                    @Override
                    public void noMatches() {
                        event.getHook().sendMessage("❌ ніхтс нєма").queue();
                    }

                    @Override
                    public void loadFailed(FriendlyException exception) {
                        event.getHook().sendMessage("❌ помілка: " + exception.getMessage()).queue();
                    }
                });
            }
            case "stop" -> {
                ServerMusicManager mgr = getOrCreateServerMusicManager(event.getGuild());
                mgr.scheduler.queue.clear();
                mgr.player.stopTrack();
                event.reply("⏹️ штоп").queue();
            }
            case "pause" -> {
                ServerMusicManager mgr = getOrCreateServerMusicManager(event.getGuild());
                boolean paused = !mgr.player.isPaused();
                mgr.player.setPaused(paused);
                event.reply(paused ? "⏸️ бауза" : "▶️ паєхалі").queue();
            }
            case "leave" -> {
                event.reply("гудбай")
                        .setEphemeral(true)
                        .flatMap(m -> event.getGuild().leave())
                        .queue();
            }
        }
    }

    private ServerMusicManager getOrCreateServerMusicManager(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        return musicManagers.computeIfAbsent(guildId, id -> {
            ServerMusicManager mgr = new ServerMusicManager(playerManager);
            guild.getAudioManager().setSendingHandler(mgr.getSendHandler());
            return mgr;
        });
    }
}
