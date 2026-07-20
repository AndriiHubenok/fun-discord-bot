package org.example.audio.youtube;

import org.example.audio.StreamingService;

import java.util.regex.Pattern;

public class YoutubeService extends StreamingService {
    private static final Pattern TRACK_PATTERN =
            Pattern.compile("https?://www\\.youtube\\.com/watch\\?v=([a-zA-Z0-9]+)");

    @Override
    public boolean isValidTrackUrl(String url) {
        return url != null && TRACK_PATTERN.matcher(url).find();
    }
}
