package me.moirai.storyengine.core.port.outbound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import me.moirai.storyengine.core.port.inbound.discord.DiscordMessageData;

public class StoryGenerationRequest {

    private final String botId;
    private final String botUsername;
    private final String botNickname;
    private final String channelId;
    private final String guildId;
    private final String adventureId;
    private final String personaId;
    private final String gameMode;
    private final String nudge;
    private final String authorsNote;
    private final String remember;
    private final String bump;
    private final int bumpFrequency;
    private final ModelConfigurationRequest modelConfiguration;
    private final ModerationConfigurationRequest moderation;
    private final List<DiscordMessageData> messageHistory;

    protected StoryGenerationRequest(Builder builder) {

        this.botId = builder.botId;
        this.botUsername = builder.botUsername;
        this.botNickname = builder.botNickname;
        this.channelId = builder.channelId;
        this.guildId = builder.guildId;
        this.adventureId = builder.adventureId;
        this.personaId = builder.personaId;
        this.gameMode = builder.gameMode;
        this.nudge = builder.nudge;
        this.authorsNote = builder.authorsNote;
        this.remember = builder.remember;
        this.bump = builder.bump;
        this.bumpFrequency = builder.bumpFrequency;
        this.modelConfiguration = builder.modelConfiguration;
        this.moderation = builder.moderation;
        this.messageHistory = Collections.unmodifiableList(builder.messageHistory);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getBotId() {
        return botId;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public String getBotNickname() {
        return botNickname;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getAdventureId() {
        return adventureId;
    }

    public String getPersonaId() {
        return personaId;
    }

    public String getGameMode() {
        return gameMode;
    }

    public String getNudge() {
        return nudge;
    }

    public String getAuthorsNote() {
        return authorsNote;
    }

    public String getRemember() {
        return remember;
    }

    public String getBump() {
        return bump;
    }

    public int getBumpFrequency() {
        return bumpFrequency;
    }

    public ModelConfigurationRequest getModelConfiguration() {
        return modelConfiguration;
    }

    public ModerationConfigurationRequest getModeration() {
        return moderation;
    }

    public List<DiscordMessageData> getMessageHistory() {
        return messageHistory;
    }

    public static final class Builder {

        private String botId;
        private String botUsername;
        private String botNickname;
        private String channelId;
        private String guildId;
        private String adventureId;
        private String personaId;
        private String gameMode;
        private String nudge;
        private String authorsNote;
        private String remember;
        private String bump;
        private int bumpFrequency;
        private ModelConfigurationRequest modelConfiguration;
        private ModerationConfigurationRequest moderation;
        private List<DiscordMessageData> messageHistory = new ArrayList<>();

        private Builder() {
        }

        public Builder channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder guildId(String guildId) {
            this.guildId = guildId;
            return this;
        }

        public Builder botUsername(String botUsername) {
            this.botUsername = botUsername;
            return this;
        }

        public Builder botId(String botId) {
            this.botId = botId;
            return this;
        }

        public Builder adventureId(String adventureId) {
            this.adventureId = adventureId;
            return this;
        }

        public Builder personaId(String personaId) {
            this.personaId = personaId;
            return this;
        }

        public Builder gameMode(String gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder modelConfiguration(ModelConfigurationRequest modelConfiguration) {
            this.modelConfiguration = modelConfiguration;
            return this;
        }

        public Builder moderation(ModerationConfigurationRequest moderation) {
            this.moderation = moderation;
            return this;
        }

        public Builder botNickname(String botNickname) {
            this.botNickname = botNickname;
            return this;
        }

        public Builder nudge(String nudge) {
            this.nudge = nudge;
            return this;
        }

        public Builder authorsNote(String authorsNote) {
            this.authorsNote = authorsNote;
            return this;
        }

        public Builder remember(String remember) {
            this.remember = remember;
            return this;
        }

        public Builder bump(String bump) {
            this.bump = bump;
            return this;
        }

        public Builder bumpFrequency(int bumpFrequency) {
            this.bumpFrequency = bumpFrequency;
            return this;
        }

        public Builder messageHistory(List<DiscordMessageData> messageHistory) {

            if (CollectionUtils.isNotEmpty(messageHistory)) {
                this.messageHistory.addAll(messageHistory);
            }

            return this;
        }

        public StoryGenerationRequest build() {
            return new StoryGenerationRequest(this);
        }
    }
}
