package org.example.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.example.api.ApiInteraction;

import java.awt.*;
import java.util.List;

public class SlashCommand {
    private final ApiInteraction apiInteraction;

    public SlashCommand(ApiInteraction apiInteraction) {
        this.apiInteraction = apiInteraction;
    }


    public EmbedBuilder getCurrencies(String[] currencies) {
        StringBuilder responseBuilder = new StringBuilder();

        for (String currency : currencies) {
            String currencyInfo = apiInteraction.getCurrency(currency);
            responseBuilder.append(currencyInfo).append("\n");
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("бігом в абмєнік");
        embed.setColor(Color.YELLOW);
        embed.setDescription(responseBuilder.toString().trim());
        return embed;
    }

    public EmbedBuilder getCat(String breed) {
        String catImageUrl = apiInteraction.getCatImage(breed);
        if (catImageUrl == null) {
            return null;
        }

        EmbedBuilder embed = new EmbedBuilder();
        switch (breed) {
            case "siam" -> embed.setTitle("чєрнаморд");
            default -> embed.setTitle("утіпуті");
        }
        embed.setColor(Color.ORANGE);
        embed.setImage(catImageUrl);
        return embed;
    }

    public EmbedBuilder getWeather(String city) {
        String weatherInfo = apiInteraction.getWeatherInfo(city);
        if (weatherInfo == null) {
            return null;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("пагодка в " + city);
        embed.setColor(Color.CYAN);
        embed.setDescription(weatherInfo);
        return embed;
    }

    public EmbedBuilder getForecast(String city, boolean detailed) {
        List<MessageEmbed.Field> fields = apiInteraction.getForecastFields(city, detailed);

        if (fields == null || fields.isEmpty()) {
            return null;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("прагноз в " + city);
        embed.setColor(Color.BLUE);

        for (MessageEmbed.Field field : fields) {
            embed.addField(field);
        }

        return embed;
    }
}
