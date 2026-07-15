package org.example.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackSchedulerTest {

    @Mock private AudioPlayer player;
    @Mock private AudioTrack track1;
    @Mock private AudioTrack track2;

    private TrackScheduler scheduler;
    private boolean disconnectCalled;

    @BeforeEach
    void setUp() {
        disconnectCalled = false;
        scheduler = new TrackScheduler(player, () -> disconnectCalled = true);
    }

    // --- queue() ---

    @Test
    void queue_WhenPlayerIsIdle_StartsTrackDirectly() {
        when(player.startTrack(track1, true)).thenReturn(true);

        scheduler.queue(track1);

        assertTrue(scheduler.queue.isEmpty(), "Трек не повинен потрапляти в чергу, якщо програвач вільний");
        verify(player).startTrack(track1, true);
    }

    @Test
    void queue_WhenPlayerIsBusy_AddsTrackToQueue() {
        when(player.startTrack(track1, true)).thenReturn(false);

        scheduler.queue(track1);

        assertEquals(1, scheduler.queue.size());
        assertTrue(scheduler.queue.contains(track1));
    }

    @Test
    void queue_MultipleTracksWhileBusy_QueuePreservesOrder() {
        when(player.startTrack(any(), eq(true))).thenReturn(false);

        scheduler.queue(track1);
        scheduler.queue(track2);

        assertEquals(2, scheduler.queue.size());
        assertSame(track1, scheduler.queue.poll());
        assertSame(track2, scheduler.queue.poll());
    }

    // --- nextTrack() ---

    @Test
    void nextTrack_WhenQueueHasTrack_PlaysIt() {
        scheduler.queue.offer(track1);

        scheduler.nextTrack();

        verify(player).startTrack(track1, false);
        assertTrue(scheduler.queue.isEmpty());
    }

    @Test
    void nextTrack_WhenQueueIsEmpty_CallsStartTrackWithNull() {
        scheduler.nextTrack();

        verify(player).startTrack(null, false);
    }

    // --- timer ---

    @Test
    void cancelTimer_WhenNoTimerStarted_DoesNotThrow() {
        assertDoesNotThrow(() -> scheduler.cancelTimer());
    }

    @Test
    void startTimer_CanBeCalledTwice_OnlyOneTaskScheduled() throws InterruptedException {
        scheduler.startTimer();
        scheduler.startTimer();

        assertDoesNotThrow(() -> scheduler.cancelTimer());
    }

    // --- onTrackStart / onTrackEnd ---

    @Test
    void onTrackStart_CancelsTimer() {
        scheduler.startTimer();
        scheduler.onTrackStart(player, track1);

        assertDoesNotThrow(() -> Thread.sleep(100));
        assertFalse(disconnectCalled);
    }

    @Test
    void onTrackEnd_WhenMayStartNext_AndQueueNotEmpty_PlaysNextTrack() {
        scheduler.queue.offer(track2);

        scheduler.onTrackEnd(player, track1, AudioTrackEndReason.FINISHED);

        verify(player).startTrack(track2, false);
    }

    @Test
    void onTrackEnd_WhenMayStartNext_AndQueueEmpty_StartsTimer() throws InterruptedException {
        scheduler.onTrackEnd(player, track1, AudioTrackEndReason.FINISHED);

        verify(player).startTrack(null, false);
    }

    @Test
    void onTrackEnd_WhenCleanupReason_DoesNotCallNextTrack() {
        scheduler.onTrackEnd(player, track1, AudioTrackEndReason.CLEANUP);

        verify(player, never()).startTrack(any(), anyBoolean());
    }
}