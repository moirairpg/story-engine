# MoirAI Story Engine
[![Java CI](https://github.com/moirairpg/story-engine/actions/workflows/automated-build.yaml/badge.svg)](https://github.com/moirairpg/story-engine/actions/workflows/automated-build.yaml)

MoirAI Story Engine is the backend for MoirAI, an AI-powered text adventure platform. It exposes a REST API and WebSocket interface consumed by the MoirAI web frontend, handling adventure and world management, AI text generation, moderation, and user authentication via Discord OAuth2.

## Technologies used
* Java 25
* Maven
* Spring Boot
* Spring Web
* Spring Data
* Spring Security
* PostgreSQL
* JTokkit
* Qdrant

## What does it do?
MoirAI Story Engine powers AI-driven text adventures. Users create worlds and adventures, configure a Narrator with a custom personality, and interact with the AI through the web frontend. The engine handles context assembly, lorebook retrieval via vector search, AI text generation and moderation, and real-time communication over WebSockets.

## Is it free?
Yes and no. The code is free to use. However, MoirAI relies on OpenAI's API for text generation and moderation, so you will need an OpenAI account and will be billed according to your usage and chosen model.

## Which AI models are supported?
MoirAI Story Engine currently supports OpenAI's GPT-5 model family.

## Building from source
To run MoirAI Story Engine locally, you will need JDK 25, Maven, a PostgreSQL database, and a Qdrant instance. A `docker-compose.yaml.sample` is provided for convenience to spin up the required infrastructure. You will also need a Discord Developer account with an OAuth2 app set up for user authentication.

### Discord OAuth2 setup
MoirAI uses Discord exclusively for user authentication. To set it up, create a Discord application in the Discord Developer dashboard and configure an OAuth2 redirect URI. By default, MoirAI Story Engine expects the redirect URI to be `localhost:8080/auth/code` — adjust this if you are running on a different host or port.

### API keys
You will need your Discord OAuth2 client ID and secret, as well as your OpenAI API key.

### Building
1. Clone the repo
2. Set up the required environment variables:
    - `DISCORD_BOT_CLIENT_ID` — your Discord OAuth2 client ID
    - `DISCORD_BOT_CLIENT_SECRET` — your Discord OAuth2 client secret
    - `DISCORD_BOT_REDIRECT_URL` — your Discord OAuth2 redirect URI
    - `OPENAI_API_TOKEN` — your OpenAI API key
    - `POSTGRES_HOST`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` — your PostgreSQL connection details
    - `QDRANT_HOST`, `QDRANT_PORT` — your Qdrant instance details
3. Set up the database (use the provided `docker-compose.yaml.sample` or bring your own)
4. Compile with `mvn clean install`
5. Run the application:
    - Through the IDE of your choice
    - Through Maven with `mvn spring-boot:run`
    - By running the JAR directly with `java -jar storyengine-3.1.0-SNAPSHOT.jar`

### With Docker
1. Clone the repo
2. Open `docker-compose.yaml.sample` and fill in your keys and tokens in the `environment` section, replacing all `CHANGE_THIS_VALUE` entries
3. Save the modified file as `docker-compose.yaml`
4. Run `docker-compose up` from the project root

## Features
* AI-powered text adventure engine with context-aware generation
* World and adventure management with full CRUD support
* Narrator configuration with custom personality per adventure
* Lorebook with vector search (RAG) for context enrichment
* AI moderation powered by OpenAI's API
* REST API consumed by the MoirAI web frontend
* Real-time adventure gameplay over WebSockets
* User authentication via Discord OAuth2
