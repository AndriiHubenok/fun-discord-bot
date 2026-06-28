package org.example;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.awt.*;

public class SlashCommandListener extends ListenerAdapter {
    private final SlashCommand slashCommand;

    public SlashCommandListener(SlashCommand slashCommand) {
        this.slashCommand = slashCommand;
    }


    @Override
    public void onGuildReady(GuildReadyEvent event) {
        event.getGuild().updateCommands().addCommands(
                Commands.slash("say", "Makes the bot say what you tell it to")
                        .addOption(OptionType.STRING, "content", "What the bot should say", true),
                Commands.slash("currency", "Get the current exchange rate for USD"),
                Commands.slash("cat", "Get a beautiful image of a cat"),
                Commands.slash("best_cat", "Get an image of best breed of cats"),
                Commands.slash("leave", "Makes the bot leave the server")
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "say" -> {
                String content = event.getOption("content", OptionMapping::getAsString);
                event.reply(content).queue();
            }
            case "currency" -> {
                event.deferReply().queue();
                String[] currencies = {"USD", "EUR"};
                EmbedBuilder result = slashCommand.getCurrencies(currencies);
                event.getHook().sendMessageEmbeds(result.build()).queue();
            }
            case "cat" -> {
                event.deferReply().queue();
                EmbedBuilder embed = slashCommand.getCat("");
                if (embed == null) {
                    event.getHook().sendMessage("а гдє").queue();
                }

                event.getHook().sendMessageEmbeds(embed.build()).queue();
            }
            case "best_cat" -> {
                event.deferReply().queue();
                EmbedBuilder embed = slashCommand.getCat("siam");
                if (embed == null) {
                    event.getHook().sendMessage("майкл нє в настроєніі").queue();
                    return;
                }

                event.getHook().sendMessageEmbeds(embed.build()).queue();
            }
            case "leave" -> {
                event.reply("гудбай")
                        .setEphemeral(true)
                        .flatMap(m -> event.getGuild().leave())
                        .queue();
            }
        }
    }
}
