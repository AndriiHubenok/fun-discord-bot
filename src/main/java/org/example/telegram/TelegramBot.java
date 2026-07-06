package org.example.telegram;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.example.audio.ServerMusicManager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Audio;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

public class TelegramBot extends TelegramLongPollingBot {
    private final AudioPlayerManager playerManager;
    private final ServerMusicManager musicManager;

    public TelegramBot(String botToken, AudioPlayerManager playerManager, ServerMusicManager musicManager) {
        super(botToken);
        this.playerManager = playerManager;
        this.musicManager = musicManager;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasAudio()) {
            Audio audio = update.getMessage().getAudio();
            String fileId = audio.getFileId();

            try {
                GetFile getFileMethod = new GetFile();
                getFileMethod.setFileId(fileId);
                org.telegram.telegrambots.meta.api.objects.File file = execute(getFileMethod);

                String safeFileName = "tg_audio_" + System.currentTimeMillis() + ".mp3";
                File localFile = downloadFile(file, new File("temp_audio/" + safeFileName));

                playerManager.loadItem(localFile.getAbsolutePath(), new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        musicManager.player.playTrack(track);
                        sendTelegramReply(update.getMessage().getChatId().toString(), "граю аудіо - " + track.getInfo().title);
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist audioPlaylist) {

                    }

                    @Override
                    public void noMatches() {

                    }

                    @Override
                    public void loadFailed(FriendlyException e) {

                    }
                });

            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendTelegramReply(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "Real Fat Shady";
    }
}
