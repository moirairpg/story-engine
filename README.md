# MoirAI Story Engine
[![Java CI](https://github.com/moirairpg/story-engine/actions/workflows/automated-build.yaml/badge.svg)](https://github.com/moirairpg/story-engine/actions/workflows/automated-build.yaml)

MoirAI Story Engine is the backend for MoirAI, an AI-powered text adventure platform. It exposes a REST API and WebSocket interface consumed by the [MoirAI Web UI](https://github.com/moirairpg/moirai-ui), handling worlds, adventures, player characters, RPG mechanics, AI narration, moderation, notifications and user authentication via Discord OAuth2.

## Technologies used
* Java 25
* Maven
* Spring Boot
* Spring Web
* Spring WebSocket (STOMP)
* Spring Data JPA
* Spring Security
* PostgreSQL
* Liquibase
* Qdrant
* S3 (MinIO locally)
* JTokkit

## What does it do?
MoirAI Story Engine powers AI-driven text adventures. Users create worlds, adventures and player characters, configure a Narrator with a custom personality, and play through the web UI, alone or with other players. The engine evaluates each player action, rolls dice when the RPG mechanics call for it, assembles the narrator's context with lorebook and memory retrieval via vector search, generates and moderates the AI's text, keeps long-term memory as chronicle summaries, and pushes every update to the players in real time over WebSockets.

## Is it free?
Yes and no. The code is free to use. However, MoirAI relies on OpenAI's API for text generation, embeddings, moderation and images, so you will need an OpenAI account and will be billed according to your usage and chosen model.

## Which AI models are supported?
MoirAI Story Engine currently supports OpenAI's GPT-5 model family.

## Building from source
To run MoirAI Story Engine locally, you will need JDK 25, Maven, a PostgreSQL database, a Qdrant instance and S3-compatible storage for images. `docker-compose-infrastructure.yaml` spins up PostgreSQL, Qdrant, MinIO and a mock of Discord for local development. A Discord application is only needed outside local development.

### Discord OAuth2 setup
MoirAI uses Discord exclusively for user authentication. Users sign up and sign in from the web UI: the engine sends the browser to an authorize page, which sends it back to the engine's callbacks, `/auth/signup/code` and `/auth/signin/code`. The callbacks only accept logins that started at the engine, so they are never opened by hand.

**Local development.** No Discord application is needed. The mock started by `docker-compose-infrastructure.yaml` on port 8081 stands in for both Discord's authorize page and its API:
- `AUTHORIZE_URL`: `http://localhost:8081/oauth2/authorize`
- `DISCORD_API_BASE_URL`: `http://localhost:8081`
- `DISCORD_BOT_SIGNIN_REDIRECT_URL` and `DISCORD_BOT_SIGNUP_REDIRECT_URL`: `http://localhost:8080/auth/signin/code` and `http://localhost:8080/auth/signup/code`
- The Discord client ID, client secret and bot token can be any value

Then sign up or sign in from the web UI. The mock's page suggests a random account name; type an existing one to log back in as that account. Use a private window or a separate browser profile for each account you want signed in at the same time.

**With Discord.** Create an application in the Discord Developer dashboard and register two OAuth2 redirect URIs, one for signing up and one for signing in, pointing at the engine's callbacks through the same host that serves the web UI. Use the same two URIs for the redirect URL variables, set `AUTHORIZE_URL` to `https://discord.com/oauth2/authorize`, leave `DISCORD_API_BASE_URL` unset, and use the application's client ID, client secret and bot token.

### API keys
You will need your OpenAI API key and, outside local development, your Discord OAuth2 client ID and secret and your Discord bot token.

### Building
1. Clone the repo
2. Set up the required environment variables:
    - `DISCORD_BOT_CLIENT_ID` — your Discord OAuth2 client ID
    - `DISCORD_BOT_CLIENT_SECRET` — your Discord OAuth2 client secret
    - `DISCORD_BOT_TOKEN` — your Discord bot token
    - `DISCORD_BOT_SIGNIN_REDIRECT_URL` — your Discord OAuth2 redirect URI for signing in
    - `DISCORD_BOT_SIGNUP_REDIRECT_URL` — your Discord OAuth2 redirect URI for signing up
    - `AUTHORIZE_URL` — the authorize page users are sent to when they sign in or sign up (`https://discord.com/oauth2/authorize`)
    - `AUTH_SUCCESS_REDIRECT_URL`, `AUTH_FAIL_REDIRECT_URL`, `AUTH_LOGOUT_REDIRECT_URL`, `AUTH_SIGNUP_REDIRECT_URL`, `AUTH_NOT_REGISTERED_REDIRECT_URL` — the web UI pages users land on after each authentication step
    - `ALLOWED_ORIGINS` — the web UI's origin (e.g. `http://localhost:5173`); only pages from it may send changes
    - `ALLOWED_HEADERS` — headers allowed in CORS requests (e.g. `Content-Type`)
    - `OPENAI_API_TOKEN` — your OpenAI API key
    - `POSTGRES_HOST`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` — your PostgreSQL connection details
    - `QDRANT_HOST`, `QDRANT_PORT` — your Qdrant instance details
    - `S3_ENDPOINT`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET`, `S3_REGION` — your S3 storage details
3. Start the infrastructure with `docker compose -f docker-compose-infrastructure.yaml up -d`, or bring your own
4. Compile with `mvn clean install`
5. Run the application:
    - Through the IDE of your choice
    - Through Maven with `mvn spring-boot:run`
    - By running the JAR directly with `java -jar storyengine-X.X.X.jar`

### With Docker
1. Clone the repo
2. Open `docker-compose.yaml.sample` and fill in your keys and tokens in the `environment` section, replacing all `CHANGE_THIS_VALUE` entries
3. Save the modified file as `docker-compose.yaml`
4. Run `docker-compose up` from the project root

## Features
* AI-powered text adventure engine with context-aware generation
* World and adventure management with full CRUD support, images and sharing
* Narrator configuration with custom personality per adventure
* Player characters with a character sheet, skills, XP and level-ups
* RPG mechanics: AI evaluation of player actions and dice checks
* Lorebook with vector search (RAG) for context enrichment
* Long-term memory through chronicle summaries
* Multiplayer adventures with invitations
* AI moderation powered by OpenAI's API
* REST API consumed by the MoirAI web UI
* Real-time adventure gameplay
