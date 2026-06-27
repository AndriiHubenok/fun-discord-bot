package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.api.ParameterStringBuilder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class SlashCommand {
    private static final String BANK_URL = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange";

    public String getCurrency(String currency){
        try {
            String urlWithParams = BANK_URL + "?json=true&valcode=" + currency;
            URI uri = new URI(urlWithParams);
            URL url = uri.toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");

            con.setRequestProperty("Accept", "application/json");

            int status = con.getResponseCode();

            if (status == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();

                String jsonResponse = content.toString();
                JsonArray jsonArray = JsonParser.parseString(jsonResponse).getAsJsonArray();

                if (!jsonArray.isEmpty()) {
                    JsonObject currencyObj = jsonArray.get(0).getAsJsonObject();

                    String currencyName = currencyObj.get("txt").getAsString();  // "Євро"
                    double rate = currencyObj.get("rate").getAsDouble();         // 51.131
                    String code = currencyObj.get("cc").getAsString();           // "EUR"

                    switch (currency) {
                        case "USD" -> currencyName = ":flag_us: :hamburger:";
                        case "EUR" -> currencyName = ":flag_eu: :rainbow_flag:";
                    }

                    return String.format("1 %s %s = %.2f UAH", code, currencyName, rate);
                } else {
                    return "Error: The bank returned an empty list.";
                }
            } else {
                System.err.println("Failed to fetch. HTTP Status: " + status);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "biba";
    }
}
