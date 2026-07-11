package org.example.audio.spotify;

public class SpotifyTrack {
    private final String id;
    private final String title;
    private final String artist;
    private final String spotifyUrl;
    private final String imageUrl;

    public SpotifyTrack(String id, String title, String artist, String spotifyUrl, String imageUrl) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.spotifyUrl = spotifyUrl;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getSpotifyUrl() {
        return spotifyUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
