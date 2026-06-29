package org.example.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApiInteraction {
    private static final String BANK_URL = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange";
    private static final String CAT_URL = "https://api.thecatapi.com/v1/images/search";
    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";

    public String getCurrency(String currency){
        try {
            LocalDate date = LocalDate.now();
            String urlWithParams = BANK_URL + "?json=true&valcode=" + currency
                    + "&date=" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
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
        return "Error getting currency.";
    }

    public String getCatImage(String breed) {
        Dotenv dotenv = Dotenv.load();
        URL url;
        try {
            if (breed.isEmpty()) {
                url = new URL(CAT_URL);
            } else {
                url = new URL(CAT_URL + "?breed_ids=" + breed);
            }
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            //con.setRequestProperty("x-api-key", dotenv.get("CAT_API_KEY"));

            int status = con.getResponseCode();

            if (status == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
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
                    JsonObject catObj = jsonArray.get(0).getAsJsonObject();
                    return catObj.get("url").getAsString();
                } else {
                    return "Error: The cat API returned an empty list.";
                }
            } else {
                System.err.println("Failed to fetch. HTTP Status: " + status);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error fetching cat image.";
    }

    public String getWeatherInfo(String city) {
        Dotenv dotenv = Dotenv.load();
        try {
            String apiKey = dotenv.get("OPEN_WEATHER_API_KEY");
            String urlWithParams = WEATHER_URL + "?q=" + city + "&appid=" + apiKey + "&units=metric";
            URL url = new URL(urlWithParams);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");

            int status = con.getResponseCode();

            if (status == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();

                String jsonResponse = content.toString();
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                return getWeatherAsString(jsonObject);

            } else {
                System.err.println("Failed to fetch. HTTP Status: " + status);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Шо за мухасранск такой - " + city;
    }

    public List<MessageEmbed.Field> getForecastFields(String city, boolean detailed) {
        Dotenv dotenv = Dotenv.load();
        try {
            String apiKey = dotenv.get("OPEN_WEATHER_API_KEY");
            String urlWithParams = FORECAST_URL + "?q=" + city + "&appid=" + apiKey + "&units=metric";
            URL url = new URL(urlWithParams);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");

            int status = con.getResponseCode();
            if (status == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();

                String jsonResponse = content.toString();
                JsonObject jsonBody = JsonParser.parseString(jsonResponse).getAsJsonObject();
                JsonArray jsonArray = jsonBody.get("list").getAsJsonArray();

                List<MessageEmbed.Field> fields = new ArrayList<>();

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                    String dtTxt = jsonObject.get("dt_txt").getAsString();

                    if (detailed) {
                        String weatherText = getWeatherAsString(jsonObject);
                        fields.add(new MessageEmbed.Field(dtTxt, weatherText, true));
                    } else if (dtTxt.contains("15:00:00")) {
                        String dateTitle = dtTxt.substring(0, 10);
                        String weatherText = getWeatherAsString(jsonObject);
                        fields.add(new MessageEmbed.Field(dateTitle, weatherText, true));
                    }
                    if (fields.size() == 25) break;
                }
                return fields;
            }
        } catch (ProtocolException e) {
            System.err.println("Protocol error: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return Collections.emptyList();
    }

    private String getWeatherAsString(JsonObject jsonObject) {
        double temperature = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();
        double feelsLike = jsonObject.getAsJsonObject("main").get("feels_like").getAsDouble();
        int humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsInt();
        int weatherId = jsonObject.getAsJsonArray("weather")
                .get(0).getAsJsonObject().get("id").getAsInt();
        int code = Integer.parseInt(Integer.toString(weatherId).substring(0, 1));
        String weatherDescription = jsonObject.getAsJsonArray("weather")
                .get(0).getAsJsonObject().get("description").getAsString();


        switch (code) {
            case 2:
                weatherDescription = "граза :thunder_cloud_rain:";
                break;
            case 3:
                weatherDescription = "марасня :white_sun_rain_cloud:";
                break;
            case 5:
                if (weatherId == 500){
                    weatherDescription = "марасня :white_sun_rain_cloud:";
                } else {
                    weatherDescription = "дощік дощік ти вже зліва, плачє груша, плачє сліва :cloud_rain:";
                }
                break;
            case 6:
                weatherDescription = "ооо зе везер аутсайд из фрайтфул :cloud_snow:";
                break;
            case 7:
                weatherDescription = "як в зайлент хілі :fog:";
                break;
            case 8:
                if (weatherId == 800) {
                    weatherDescription = "солнечній урод свєтіт :sun_with_face:";
                } else if (weatherId == 801) {
                    weatherDescription = "трохі хмар :white_sun_small_cloud:";
                } else if (weatherId == 802) {
                    weatherDescription = "розсєяні хмари :partly_sunny:";
                } else if (weatherId == 803) {
                    weatherDescription = "разбиті хмари :white_sun_cloud:";
                }  else if (weatherId == 804) {
                    weatherDescription = "тупа скайрім :cloud:";
                }

        }
        return String.format("тємпєратурка - %.1f°C\nащущаєтся как - %.1f°C\nжідєнькость - %d%%\nопіс: %s",
                temperature, feelsLike, humidity, weatherDescription);
    }
}
