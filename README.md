# Real Fat Shady: Your Server's Companion

## Description
"Real Fat Shady" is a versatile, multi-purpose bot built in Java. It serves as an all-in-one companion for both **Discord servers** and **Telegram chats**, providing real-time currency exchange rates alongside a robust audio system capable of streaming tracks directly from Spotify. By leveraging Discord's Slash Commands and the Telegram Bot API, it offers a seamless and modern user experience across two platforms.

## Motivation
It is hard to find a discord bot that can play music from YouTube without a problems or "Premium" subscription. Also, I wanted to implement features that i had never seen in other bots but which was important enough to me and my friends (Playing music from Telegram, weather forecast, currency, etc.). This project was built to consolidate those features into a single, highly responsive application. It also serves as a practical implementation of concurrent asynchronous task handling, third-party API integration (Spotify, currency exchange, cats pictures, Telegram, etc.).

## Features
* **Real-Time Currency Exchange:** Fetches and parses live exchange rates using external APIs, delivering accurate financial data on demand.
* **Spotify Audio Integration:** Seamlessly searches and queues tracks directly from Spotify, handled by a robust custom music manager.
* **Slash Command Architecture:** Fully utilizes Discord's Slash Command API for intuitive and auto-completing user commands.
* **Telegram Bot Integration:** A companion Telegram bot that can download audio files and playing directly inside Discord bot, powered by the Telegram Bot API.
* **Continuous Integration:** Automated build and testing pipelines configured via GitHub Actions.

## Quick Start

### Prerequisites
Before you begin, ensure you have the following installed and configured:
* **Java Development Kit (JDK):** Version 17 or higher recommended.
* **Maven:** For dependency management and building the project.
* **Discord Application:** A registered bot with its token from the Discord Developer Portal.
* **Spotify API Credentials:** Client ID and Secret from the Spotify Developer Dashboard.
* **Telegram Bot Token:** A bot token obtained from [@BotFather](https://t.me/BotFather) on Telegram.

### Installation
1. **Clone the repository:**
```
git clone https://github.com/andriihubenok/fun-discord-bot.git
cd fun-discord-bot
```

2. **Configure your environment variables:**
Copy the example environment file and fill in your secure credentials.
```bash
cp .env.example .env
```

*Open `.env` and add your Discord Bot Token, Spotify API keys, Currency API keys, and Telegram Bot Token.*

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

### Discord
Once the bot is invited to your server and running, you can interact with it using slash commands. Type `/` in any text channel to see the available commands.

* **`/currency`** - Retrieves the latest exchange rate of USD and EUR to UAH.
* **`/play [url]`** - Adds a track to the server's audio queue and starts playback.
* **`/skip`** - Skips the currently playing track.
* **`/stop`** - Halts audio playback and clears the queue.
* **`/cat`** - Get a picture of a cat.
* **`/weather [city]`** - Get a current weather for a specific city.
* **`/forecast_detailed [city]`** - Get a weather forecast for a specific city for 5 days.
* **`/broadcast_tg`** - Starting to listening for a Telegram uploading music.

### Telegram
Once the Telegram bot is running, you can interact with it directly in any Telegram chat. Just upload an audiofile and use `/broadcast_tg` command in Discord.

## Contributing

Contributions are always welcome! If you'd like to improve the codebase, add new APIs, or fix a bug:

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request
