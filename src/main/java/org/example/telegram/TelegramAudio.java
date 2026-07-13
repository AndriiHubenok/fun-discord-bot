package org.example.telegram;

public class TelegramAudio {
    private String id;
    private String name;
    private String title;
    private String performer;
    private int duration;
    private String filePath;

    public TelegramAudio(String id, String name, String title, String performer, int duration) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.performer = performer;
        this.duration = duration;
    }

    public TelegramAudio(String id, String name, int duration) {
        this.id = id;
        this.name = name;
        this.duration = duration;
    }

    public TelegramAudio(String id, int duration) {
        this.id = id;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
