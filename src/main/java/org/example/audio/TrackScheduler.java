package org.example.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.Queue;
import java.util.concurrent.*;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    public final Queue<AudioTrack> queue;
    private final ScheduledExecutorService timer;
    private ScheduledFuture<?> disconnectTask;
    private final Runnable disconnectAction;

    public TrackScheduler(AudioPlayer player, Runnable disconnectAction) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.disconnectAction = disconnectAction;
        timer = Executors.newSingleThreadScheduledExecutor();
    }

    public void startTimer() {
        if (disconnectTask == null || disconnectTask.isDone()) {
            disconnectTask = timer.schedule(disconnectAction, 5, TimeUnit.MINUTES);
        }
    }

    public void cancelTimer() {
        if (disconnectTask != null) {
            disconnectTask.cancel(false);
            disconnectTask = null;
        }
    }

    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public void nextTrack() {
        player.startTrack(queue.poll(), false);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        cancelTimer();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }

        if (queue.isEmpty()) {
            startTimer();
        }
    }
}
