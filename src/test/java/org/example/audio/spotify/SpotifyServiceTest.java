package org.example.audio.spotify;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class SpotifyServiceTest {

    private static final Pattern TRACK_PATTERN =
            Pattern.compile("https?://open\\.spotify\\.com/track/([a-zA-Z0-9]+)");

    boolean isSpotifyTrackUrl(String url) {
        return url != null && TRACK_PATTERN.matcher(url).find();
    }

    @ParameterizedTest
    @CsvSource({
            "https://open.spotify.com/track/3n3Ppam7vgaVa1iaRUIOKE, true",
            "http://open.spotify.com/track/abc123XYZ, true",
            "https://open.spotify.com/track/3n3Ppam7vgaVa1iaRUIOKE?si=xxx, true"
    })
    void isSpotifyTrackUrl_ValidUrls_ReturnsTrue(String url, boolean expected) {
        assertEquals(expected, isSpotifyTrackUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://open.spotify.com/album/abc123",
            "https://youtube.com/watch?v=dQw4w9WgXcQ",
            "not a url at all",
            "https://open.spotify.com/track/"
    })
    void isSpotifyTrackUrl_InvalidUrls_ReturnsFalse(String url) {
        assertFalse(isSpotifyTrackUrl(url));
    }

    @ParameterizedTest
    @NullSource
    void isSpotifyTrackUrl_Null_ReturnsFalse(String url) {
        assertFalse(isSpotifyTrackUrl(url));
    }
}
