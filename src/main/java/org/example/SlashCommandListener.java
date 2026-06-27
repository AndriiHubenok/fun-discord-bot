package org.example;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class SlashCommandListener extends ListenerAdapter {
    private SlashCommand slashCommand;

    public SlashCommandListener() {
        slashCommand = new SlashCommand();
    }


    @Override
    public void onGuildReady(GuildReadyEvent event) {
        event.getGuild().updateCommands().addCommands(
                Commands.slash("say", "Makes the bot say what you tell it to")
                        .addOption(OptionType.STRING, "content", "What the bot should say", true),
                Commands.slash("currency", "Get the current exchange rate for USD"),
                Commands.slash("cat", "Get a beautiful image of a cat"),
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
                String resultUsd = slashCommand.getCurrency("USD");
                String resultEur = slashCommand.getCurrency("EUR");
                event.getHook().sendMessage("бігом в абмєннік\n" + resultUsd + "\n" + resultEur).queue();
            }
            case "cat" -> {
                event.deferReply().queue();
                String catImageUrl = slashCommand.getCatImage();
                event.getHook().sendMessage(catImageUrl).queue();
            }
            case "leave" -> {
                event.reply("I'm leaving the server now!")
                        .setEphemeral(true)
                        .flatMap(m -> event.getGuild().leave())
                        .queue();
            }
        }
    }
}
