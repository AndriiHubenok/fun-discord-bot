package org.example.discord;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.example.audio.ServerMusicManager;
import org.example.audio.spotify.SpotifyTrack;

import java.awt.*;

public class AudioLoadResultHandlerImpl implements AudioLoadResultHandler {
    private final SlashCommandInteractionEvent event;
    private final ServerMusicManager musicManager;
    private final String finalUrl;
    private final SpotifyTrack finalSpotifyTrack;

    public AudioLoadResultHandlerImpl(SlashCommandInteractionEvent event, ServerMusicManager musicManager, String url, SpotifyTrack spotifyTrack) {
        this.event = event;
        this.musicManager = musicManager;
        this.finalUrl = url;
        this.finalSpotifyTrack = spotifyTrack;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        addTrackToQueue(track);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        AudioTrack track;
        if (playlist.getTracks().size() > 1 && !finalUrl.startsWith("ytsearch:")) {
            playlist.getTracks().forEach(this::addTrackToQueue);
            return;
        } else {
            track = playlist.getTracks().getFirst();
        }
        addTrackToQueue(track);
    }

    @Override
    public void noMatches() {
        event.getHook().sendMessage("❌ ніхтс нєма").queue();
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        event.getHook().sendMessage("❌ помілка: " + exception.getMessage()).queue();
    }

    private void addTrackToQueue(AudioTrack track){
        musicManager.scheduler.queue(track);

        EmbedBuilder embed = new EmbedBuilder();

        if (finalSpotifyTrack != null) {
            embed.setColor(Color.decode("#1ed760"));
            embed.setTitle(finalSpotifyTrack.getTitle(), finalSpotifyTrack.getSpotifyUrl());
            embed.addField("іспалняєт:", String.format("***%s***", finalSpotifyTrack.getArtist()), false);
            embed.setThumbnail(finalSpotifyTrack.getImageUrl());
            embed.setFooter("работаєм с Spotify", "https://upload.wikimedia.org/wikipedia/commons/thumb/1/19/Spotify_logo_without_text.svg/960px-Spotify_logo_without_text.svg.png");
        } else {
            embed.setColor(Color.decode("#d9252a"));
            embed.setTitle(track.getInfo().title, track.getInfo().uri);
            embed.addField("канал/автор:", String.format("***%s***", track.getInfo().author), false);
            embed.setFooter("работаєм с YouTube", "https://upload.wikimedia.org/wikipedia/commons/6/67/YouTube_Logo_June.png?_=20260623194452");

            if (track.getInfo().uri.contains("youtube.com")) {
                embed.setThumbnail("https://img.youtube.com/vi/" + track.getInfo().identifier + "/mqdefault.jpg");
            }
        }

        long duration = track.getInfo().length;
        addDurationFieldToEmbed(embed, duration, false);


        if (!musicManager.scheduler.queue.isEmpty()) {
            if (musicManager.scheduler.currentTrack != null) {
                duration += musicManager.scheduler.currentTrack.getDuration();
            }
            if (musicManager.scheduler.queue.size() > 1) {
                duration += musicManager.scheduler.queue.stream()
                        .reduce(0L, (sum, t) -> sum + t.getDuration(), Long::sum) - track.getInfo().length;
            }
            addDurationFieldToEmbed(embed, duration, true);
        }

        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    private void addDurationFieldToEmbed(EmbedBuilder embed, long duration, boolean isQueue) {
        long seconds = duration % 60000 / 1000;
        long minutes = duration / 60000;
        long hours = minutes / 60;
        if (hours > 0) {
            minutes -= hours * 60;
            if (isQueue) {
                embed.addField("врємя очєрєді: ", String.format("`%d:%02d:%02d`", hours, minutes, seconds), false);
            } else {
                embed.addField("врємя: ", String.format("`%d:%02d:%02d`", hours, minutes, seconds), false);
            }
        } else {
            if (isQueue) {
                embed.addField("врємя очєрєді: ", String.format("`%02d:%02d`", minutes, seconds), false);
            } else {
                embed.addField("врємя: ", String.format("`%02d:%02d`", minutes, seconds), false);
            }
        }
    }
}
