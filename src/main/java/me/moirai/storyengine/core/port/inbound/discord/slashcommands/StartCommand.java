package me.moirai.storyengine.core.port.inbound.discord.slashcommands;

import me.moirai.storyengine.common.usecases.UseCase;
import reactor.core.publisher.Mono;

public final class StartCommand extends UseCase<Mono<Void>> {

    private final String botId;
    private final String channelId;
    private final String guildId;
    private final String botNickname;
    private final String botUsername;

    private StartCommand(Builder builder) {

        this.channelId = builder.channelId;
        this.guildId = builder.guildId;
        this.botId = builder.botId;
        this.botNickname = builder.botNickname;
        this.botUsername = builder.botUsername;
    }

    public static Builder builder() {

        return new Builder();
    }

    public String getChannelId() {
        return channelId;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getBotId() {
        return botId;
    }

    public String getBotNickname() {
        return botNickname;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public static final class Builder {

        private String botId;
        private String channelId;
        private String guildId;
        private String botNickname;
        private String botUsername;

        private Builder() {
        }

        public Builder botId(String botId) {
            this.botId = botId;
            return this;
        }

        public Builder channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder guildId(String guildId) {
            this.guildId = guildId;
            return this;
        }

        public Builder botNickname(String botNickname) {
            this.botNickname = botNickname;
            return this;
        }

        public Builder botUsername(String botUsername) {
            this.botUsername = botUsername;
            return this;
        }

        public StartCommand build() {
            return new StartCommand(this);
        }
    }
}
