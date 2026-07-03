package org.example.audio.spotify;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyService {
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String TRACK_URL = "https://api.spotify.com/v1/tracks/";
    private static final Pattern TRACK_PATTERN =
            Pattern.compile("https?://open\\.spotify\\.com/track/([a-zA-Z0-9]+)");

    private final String clientId;
    private final String clientSecret;
    private final HttpClient httpClient;
    private final Gson gson;

    private String accessToken;
    private Instant accessTokenExpiresAt;

    public SpotifyService() {
        Dotenv dotenv = Dotenv.load();
        this.clientId = dotenv.get("SPOTIFY_CLIENT_ID");
        this.clientSecret = dotenv.get("SPOTIFY_CLIENT_SECRET");
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public boolean isSpotifyTrackUrl(String url) {
        return url != null && TRACK_PATTERN.matcher(url).find();
    }

    public String convertTrackUrlToYoutubeSearch(String spotifyUrl) throws IOException, InterruptedException {
        String trackId = extractTrackId(spotifyUrl);
        if (trackId == null) {
            throw new IllegalArgumentException("Invalid Spotify track URL");
        }

        SpotifyTrack track = getTrack(trackId);
        String query = track.getArtist() + " - " + track.getTitle();
        return "ytsearch:" + query;
    }

    public SpotifyTrack getTrack(String trackId) throws IOException, InterruptedException {
        String token = getValidAccessToken();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TRACK_URL + URLEncoder.encode(trackId, StandardCharsets.UTF_8)))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Spotify track request failed: " + response.statusCode() + " - " + response.body());
        }

        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        String title = json.get("name").getAsString();

        JsonArray artists = json.getAsJsonArray("artists");
        String artist = artists.size() > 0
                ? artists.get(0).getAsJsonObject().get("name").getAsString()
                : "Unknown Artist";

        String externalUrl = json.getAsJsonObject("external_urls").get("spotify").getAsString();

        return new SpotifyTrack(trackId, title, artist, externalUrl);
    }

    private String extractTrackId(String spotifyUrl) {
        Matcher matcher = TRACK_PATTERN.matcher(spotifyUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getValidAccessToken() throws IOException, InterruptedException {
        if (accessToken != null && accessTokenExpiresAt != null &&
                Instant.now().isBefore(accessTokenExpiresAt.minusSeconds(30))) {
            return accessToken;
        }

        String credentials = clientId + ":" + clientSecret;
        String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Authorization", "Basic " + basicAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Spotify token request failed: " + response.statusCode() + " - " + response.body());
        }

        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        accessToken = json.get("access_token").getAsString();
        long expiresIn = json.get("expires_in").getAsLong();
        accessTokenExpiresAt = Instant.now().plusSeconds(expiresIn);

        return accessToken;
    }
}
