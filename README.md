# Real Fat Shady: Your Server's Companion

## Description
"Real Fat Shady" is a versatile, multi-purpose Discord bot built in Java. It serves as an all-in-one companion for Discord servers, providing real-time currency exchange rates alongside a robust audio system capable of streaming tracks directly from Spotify. By leveraging Discord's Slash Commands, it offers a seamless and modern user experience. 

## Motivation
It is hard to find a discord bot that can play music from YouTube without a problems or "Premium" subscription. Also, i wanted to implement features that i had never seen in other bots but which was important enough to me and my friends (weather forecast, currency, etc.). This project was built to consolidate those features into a single, highly responsive application. It also serves as a practical implementation of concurrent asynchronous task handling, third-party API integration (Spotify, currency exchange, cats pictures, etc.).

## Features
* **Real-Time Currency Exchange:** Fetches and parses live exchange rates using external APIs, delivering accurate financial data on demand.
* **Spotify Audio Integration:** Seamlessly searches and queues tracks directly from Spotify, handled by a robust custom music manager.
* **Slash Command Architecture:** Fully utilizes Discord's Slash Command API for intuitive and auto-completing user commands.
* **Continuous Integration:** Automated build and testing pipelines configured via GitHub Actions.

## Quick Start

### Prerequisites
Before you begin, ensure you have the following installed and configured:
* **Java Development Kit (JDK):** Version 17 or higher recommended.
* **Maven:** For dependency management and building the project.
* **Discord Application:** A registered bot with its token from the Discord Developer Portal.
* **Spotify API Credentials:** Client ID and Secret from the Spotify Developer Dashboard.

### Installation
1. **Clone the repository:**
```
git clone [https://github.com/andriihubenok/fun-discord-bot.git](https://github.com/andriihubenok/fun-discord-bot.git)
cd fun-discord-bot
```


2. **Configure your environment variables:**
Copy the example environment file and fill in your secure credentials.
```bash
cp .env.example .env

```


*Open `.env` and add your Discord Bot Token, Spotify API keys, and Currency API keys.*
3. **Build the project:**
Use Maven to download dependencies and build the application.
```bash
mvn clean install

```


4. **Run the bot:**
```bash
mvn exec:java -Dexec.mainClass="org.example.Main"

```



## Usage

Once the bot is invited to your server and running, you can interact with it using slash commands. Type `/` in any text channel to see the available commands.

* **`/currency`** - Retrieves the latest exchange rate of USD and EUR to UAH.
* **`/play [url]`** - Adds a track to the server's audio queue and starts playback.
* **`/skip`** - Skips the currently playing track.
* **`/stop`** - Halts audio playback and clears the queue.
* **`/cat`** - Get a picture of a cat.
* **`/weather [city]`** - Get a current weather for a specific city.
* **`/weather_detailed [city]`** - Get a weather forecast for a specific city for 5 days. 

## Contributing

Contributions are always welcome! If you'd like to improve the codebase, add new APIs, or fix a bug:

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request
