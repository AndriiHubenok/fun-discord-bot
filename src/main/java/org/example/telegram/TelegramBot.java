package org.example.telegram;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import org.example.audio.ServerMusicManager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Audio;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TelegramBot extends TelegramLongPollingBot {
    private final AudioPlayerManager playerManager;
    private final Map<String, ServerMusicManager> pendingPins;
    private final Map<Long, ServerMusicManager> activeTgSessions;
    private final ScheduledExecutorService scheduler;


    public TelegramBot(String botToken, AudioPlayerManager playerManager,
                       Map<String, ServerMusicManager> pendingPins,
                       Map<Long, ServerMusicManager> activeTgSessions) {
        super(botToken);
        this.playerManager = playerManager;
        this.pendingPins = pendingPins;
        this.activeTgSessions = activeTgSessions;
        this.scheduler = Executors.newScheduledThreadPool(1);
        registerCommands();
    }

    public void registerCommands() {
        List<BotCommand> commands = List.of(
                new BotCommand("pin", "Bind Telegram chat to pending session"),
                new BotCommand("help", "Show available commands")
        );

        try {
            execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = update.getMessage().getChatId();
        if (!update.hasMessage()) return;

        if (update.getMessage().hasText()) {
        String text = update.getMessage().getText();

            if (text.startsWith("/pin")) {
                String query = text.substring(5).trim();
                if (!query.isEmpty()) pinCommand(chatId, query);
            }

        } else if (update.getMessage().hasAudio()) {
            TelegramAudio audio = new TelegramAudio(
                    update.getMessage().getAudio().getFileId(),
                    update.getMessage().getAudio().getFileName(),
                    update.getMessage().getAudio().getTitle(),
                    update.getMessage().getAudio().getPerformer(),
                    update.getMessage().getAudio().getDuration()
            );
            audioCommand(chatId, audio);
        } else if (update.getMessage().hasVideo()) {
            TelegramAudio audio = new TelegramAudio(
                    update.getMessage().getVideo().getFileId(),
                    update.getMessage().getVideo().getFileName(),
                    update.getMessage().getVideo().getDuration()
            );
            audioCommand(chatId, audio);
        } else if (update.getMessage().hasVoice()) {
            TelegramAudio audio = new TelegramAudio(
                    update.getMessage().getVoice().getFileId(),
                    update.getMessage().getVoice().getDuration()
            );
            audioCommand(chatId, audio);
        } else if (update.getMessage().hasVideoNote()) {
            TelegramAudio audio = new TelegramAudio(
                    update.getMessage().getVideoNote().getFileId(),
                    update.getMessage().getVideoNote().getDuration()
            );
            audioCommand(chatId, audio);
        }
    }

    @Override
    public String getBotUsername() {
        return "Real Fat Shady";
    }

    private void sendTelegramReply(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void pinCommand(Long chatId, String pin) {
        ServerMusicManager musicManager = pendingPins.get(pin);
        if (musicManager != null) {
            activeTgSessions.put(chatId, musicManager);
            pendingPins.remove(pin);
            sendTelegramReply(chatId, "работаєм, кідай сюда рєп, відєа, галасавухи тєлак");
        } else {
            sendTelegramReply(chatId, "єє нєєєє, єт чо");
        }
    }

    private void audioCommand(Long chatId, TelegramAudio audio) {
        if (!activeTgSessions.containsKey(chatId)) {
            sendTelegramReply(chatId, "єє нєєєє, я хачу сінхранізацию с тг, так сказать");
            return;
        }
        ServerMusicManager musicManager = activeTgSessions.get(chatId);

        if (audio.getId() != null) {
            try {
                GetFile getFileMethod = new GetFile();
                getFileMethod.setFileId(audio.getId());
                org.telegram.telegrambots.meta.api.objects.File file = execute(getFileMethod);

                String tgFilePath = file.getFilePath();
                String extension = ".mp3";
                if (tgFilePath != null && tgFilePath.contains(".")) {
                    extension = tgFilePath.substring(tgFilePath.lastIndexOf("."));
                    if (extension.equals(".oga")) extension = ".ogg";
                }

                String safeFileName = "tg_audio_" + System.currentTimeMillis() + extension;
                File localFile = downloadFile(file, new File("temp_audio/" + safeFileName));

                if (localFile.length() == 0) {
                    sendTelegramReply(chatId, "тэлэга передала порожній файл.");
                    return;
                }

                scheduler.schedule(() -> {
                    try {
                        Files.delete(localFile.toPath());
                        System.out.println("Deleted temporary file: " + localFile.getName());
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println("Failed to delete temporary file: " + localFile.getName());
                    }
                }, 7200L + audio.getDuration(), TimeUnit.SECONDS);


                playerManager.loadItem(localFile.getAbsolutePath(), new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        musicManager.scheduler.queue(track);

                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setColor(Color.decode("#1d94cf"));
                        if (audio.getTitle() != null) {
                            embed.setTitle(audio.getTitle());
                        } else {
                            embed.setTitle(audio.getName());
                        }
                        if (audio.getPerformer() != null) {
                            embed.addField("іспалняєт:", String.format("***%s***", audio.getPerformer()), false);
                        }
                        long seconds = audio.getDuration() % 60;
                        long minutes = audio.getDuration() / 60;
                        long hours = minutes / 60;
                        if (hours > 0) {
                            minutes -= hours * 60;
                            embed.addField("врємя: ", String.format("`%d:%02d:%02d`", hours, minutes, seconds), false);
                        } else {
                            embed.addField("врємя: ", String.format("`%02d:%02d`", minutes, seconds), false);
                        }

                        if (!musicManager.scheduler.queue.isEmpty()) {
                            long duration = audio.getDuration();
                            if (musicManager.scheduler.currentTrack != null) {
                                duration += musicManager.scheduler.currentTrack.getDuration();
                            }
                            if (musicManager.scheduler.queue.size() > 0) {
                                duration += musicManager.scheduler.queue.stream()
                                        .reduce(0L, (sum, t) -> sum + t.getDuration(), Long::sum) - audio.getDuration();
                            }
                            seconds = duration % 60000 / 1000;
                            minutes = duration / 60000;
                            hours = minutes / 60;
                            if (hours > 0) {
                                minutes -= hours * 60;
                                embed.addField("врємя очєрєді: ", String.format("`%d:%02d:%02d`", hours, minutes, seconds), false);
                            } else {
                                embed.addField("врємя очєрєді: ", String.format("`%02d:%02d`", minutes, seconds), false);
                            }
                        }

                        embed.setFooter("работаєм с Telegram", "https://upload.wikimedia.org/wikipedia/commons/thumb/8/83/Telegram_2019_Logo.svg/960px-Telegram_2019_Logo.svg.png");

                        musicManager.lastCommandChannel.sendMessageEmbeds(embed.build()).queue();
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist audioPlaylist) {}

                    @Override
                    public void noMatches() {
                        sendTelegramReply(chatId, "❌ дєскорд не зміг розпізнати формат файлу.");
                    }

                    @Override
                    public void loadFailed(FriendlyException e) {
                        sendTelegramReply(chatId, "❌ помілка завантаження " + e.getMessage());
                    }
                });

            } catch (TelegramApiException e) {
                e.printStackTrace();
                sendTelegramReply(chatId, "❌ помилка завантаження з тєлєграми");
            }
        }
    }
}
