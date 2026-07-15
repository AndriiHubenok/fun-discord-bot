package org.example.audio.spotify;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpotifyTrackTest {

    @Test
    void constructor_SetsAllFields() {
        SpotifyTrack track = new SpotifyTrack(
                "3n3Ppam7vgaVa1iaRUIOKE",
                "Money Trees",
                "Kendrick Lamar",
                "https://open.spotify.com/track/3n3Ppam7vgaVa1iaRUIOKE",
                "https://i.scdn.co/image/abc123.jpg"
        );

        assertEquals("3n3Ppam7vgaVa1iaRUIOKE", track.getId());
        assertEquals("Money Trees", track.getTitle());
        assertEquals("Kendrick Lamar", track.getArtist());
        assertEquals("https://open.spotify.com/track/3n3Ppam7vgaVa1iaRUIOKE", track.getSpotifyUrl());
        assertEquals("https://i.scdn.co/image/abc123.jpg", track.getImageUrl());
    }

    @Test
    void constructor_WithNullFields_DoesNotThrow() {
        assertDoesNotThrow(() -> new SpotifyTrack(null, null, null, null, null));
    }

    @Test
    void getTitle_ReturnsCorrectValue() {
        SpotifyTrack track = new SpotifyTrack("id", "HUMBLE.", "Kendrick Lamar", "url", "img");
        assertEquals("HUMBLE.", track.getTitle());
    }
}