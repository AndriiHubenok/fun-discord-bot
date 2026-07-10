package org.example.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class ServerMusicManager {
    public final AudioPlayer player;
    public final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;
    public MessageChannel lastCommandChannel;

    public ServerMusicManager(AudioPlayerManager manager, Guild guild) {
        this.player = manager.createPlayer();

        Runnable disconnectAction = () -> {
            guild.getAudioManager().closeAudioConnection();
            if (lastCommandChannel != null) {
                lastCommandChannel.sendMessage(":zzz: пашов спати").queue();
            }
        };

        this.scheduler = new TrackScheduler(player, disconnectAction);
        this.player.addListener(scheduler);
        this.sendHandler = new AudioPlayerSendHandler(player);

        this.scheduler.startTimer();
    }

    public AudioPlayerSendHandler getSendHandler() {
        return sendHandler;
    }
}
