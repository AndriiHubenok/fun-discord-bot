package org.example.telegram;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TelegramAudioTest {

    @Test
    void fullConstructor_SetsAllFields() {
        TelegramAudio audio = new TelegramAudio("id1", "file.mp3", "Song Title", "Artist", 180);

        assertEquals("id1", audio.getId());
        assertEquals("file.mp3", audio.getName());
        assertEquals("Song Title", audio.getTitle());
        assertEquals("Artist", audio.getPerformer());
        assertEquals(180, audio.getDuration());
        assertNull(audio.getFilePath(), "filePath має бути null після конструктора");
    }

    @Test
    void shortConstructor_WithNameAndDuration_SetsCorrectly() {
        TelegramAudio audio = new TelegramAudio("id2", "audio.ogg", 90);

        assertEquals("id2", audio.getId());
        assertEquals("audio.ogg", audio.getName());
        assertEquals(90, audio.getDuration());
        assertNull(audio.getTitle());
        assertNull(audio.getPerformer());
    }

    @Test
    void minimalConstructor_OnlyIdAndDuration() {
        TelegramAudio audio = new TelegramAudio("id3", 60);

        assertEquals("id3", audio.getId());
        assertEquals(60, audio.getDuration());
        assertNull(audio.getName());
    }

    @Test
    void setFilePath_UpdatesFilePath() {
        TelegramAudio audio = new TelegramAudio("id4", 30);
        audio.setFilePath("/downloads/audio.mp3");

        assertEquals("/downloads/audio.mp3", audio.getFilePath());
    }

    @Test
    void setters_UpdateAllFields() {
        TelegramAudio audio = new TelegramAudio("old_id", 10);
        audio.setId("new_id");
        audio.setName("new_name.mp3");
        audio.setTitle("New Title");
        audio.setPerformer("New Artist");
        audio.setDuration(999);

        assertEquals("new_id", audio.getId());
        assertEquals("new_name.mp3", audio.getName());
        assertEquals("New Title", audio.getTitle());
        assertEquals("New Artist", audio.getPerformer());
        assertEquals(999, audio.getDuration());
    }
}
