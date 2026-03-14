package me.moirai.storyengine.core.domain.adventure;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import me.moirai.storyengine.common.annotation.UuidIdentifier;
import me.moirai.storyengine.common.domain.Permissions;
import me.moirai.storyengine.common.domain.ShareableAsset;
import me.moirai.storyengine.common.domain.Visibility;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@Entity(name = "Adventure")
@Table(name = "adventure")
public class Adventure extends ShareableAsset {

    @Id
    @UuidIdentifier
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "world_id", nullable = false)
    private String worldId;

    @Column(name = "persona_id", nullable = false)
    private String personaId;

    @Column(name = "channel_id", nullable = false)
    private String channelId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "adventure_start", nullable = false)
    private String adventureStart;

    @Column(name = "is_multiplayer", nullable = false)
    private boolean isMultiplayer;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_mode", nullable = false)
    private GameMode gameMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation", nullable = false)
    private Moderation moderation;

    @Embedded
    private ContextAttributes contextAttributes;

    @Embedded
    private ModelConfiguration modelConfiguration;

    private Adventure(Builder builder) {

        super(builder.creatorId, builder.creationDate,
                builder.lastUpdateDate, builder.permissions, builder.visibility, builder.version);

        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.adventureStart = builder.adventureStart;
        this.worldId = builder.worldId;
        this.personaId = builder.personaId;
        this.channelId = builder.channelId;
        this.contextAttributes = builder.contextAttributes;
        this.modelConfiguration = builder.modelConfiguration;
        this.moderation = builder.moderation;
        this.gameMode = builder.gameMode;
        this.isMultiplayer = builder.isMultiplayer;
    }

    protected Adventure() {
        super();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAdventureStart() {
        return adventureStart;
    }

    public String getWorldId() {
        return worldId;
    }

    public String getPersonaId() {
        return personaId;
    }

    public String getChannelId() {
        return channelId;
    }

    public ModelConfiguration getModelConfiguration() {
        return modelConfiguration;
    }

    public Moderation getModeration() {
        return moderation;
    }

    public ContextAttributes getContextAttributes() {
        return contextAttributes;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    public static Builder builder() {

        return new Builder();
    }

    public void updateName(String name) {

        this.name = name;
    }

    public void updateDescription(String description) {

        this.description = description;
    }

    public void updateAdventureStart(String adventureStart) {

        this.adventureStart = adventureStart;
    }

    public void updatePersona(String personaId) {

        this.personaId = personaId;
    }

    public void updateWorld(String worldId) {

        this.worldId = worldId;
    }

    public void updateChannel(String channelId) {

        this.channelId = channelId;
    }

    public void updateModeration(Moderation moderation) {

        this.moderation = moderation;
    }

    public void updateGameMode(GameMode gameMode) {

        this.gameMode = gameMode;
    }

    public void updateAiModel(ArtificialIntelligenceModel aiModel) {

        ModelConfiguration newModelConfiguration = this.modelConfiguration.updateAiModel(aiModel);
        this.modelConfiguration = newModelConfiguration;
    }

    public void updateMaxTokenLimit(int maxTokenLimit) {

        ModelConfiguration newModelConfiguration = this.modelConfiguration.updateMaxTokenLimit(maxTokenLimit);
        this.modelConfiguration = newModelConfiguration;
    }

    public void updateTemperature(double temperature) {

        ModelConfiguration newModelConfiguration = this.modelConfiguration.updateTemperature(temperature);
        this.modelConfiguration = newModelConfiguration;
    }

    public void updateFrequencyPenalty(double frequencyPenalty) {

        ModelConfiguration newModelConfiguration = this.modelConfiguration.updateFrequencyPenalty(frequencyPenalty);
        this.modelConfiguration = newModelConfiguration;
    }

    public void updatePresencePenalty(double presencePenalty) {

        ModelConfiguration newModelConfiguration = this.modelConfiguration.updatePresencePenalty(presencePenalty);
        this.modelConfiguration = newModelConfiguration;
    }

    public void addStopSequence(String stopSequence) {

        ModelConfiguration newModelConfiguration = this.modelConfiguration.addStopSequence(stopSequence);
        this.modelConfiguration = newModelConfiguration;
    }

    public void removeStopSequence(String stopSequence) {

        ModelConfiguration newModelConfiguration = this.modelConfiguration.removeStopSequence(stopSequence);
        this.modelConfiguration = newModelConfiguration;
    }

    public void addLogitBias(String token, double bias) {

        ModelConfiguration newModelConfiguration = this.modelConfiguration.addLogitBias(token, bias);
        this.modelConfiguration = newModelConfiguration;
    }

    public void removeLogitBias(String token) {

        ModelConfiguration newModelConfiguration = this.modelConfiguration.removeLogitBias(token);
        this.modelConfiguration = newModelConfiguration;
    }

    public void makeMultiplayer() {
        this.isMultiplayer = true;
    }

    public void makeSinglePlayer() {
        this.isMultiplayer = false;
    }

    public void updateNudge(String nudge) {

        ContextAttributes newContextAttributes = this.contextAttributes.updateNudge(nudge);
        this.contextAttributes = newContextAttributes;
    }

    public void updateBump(String bump) {

        ContextAttributes newContextAttributes = this.contextAttributes.updateBump(bump);
        this.contextAttributes = newContextAttributes;
    }

    public void updateBumpFrequency(Integer bumpFrequency) {

        ContextAttributes newContextAttributes = this.contextAttributes.updateBumpFrequency(bumpFrequency);
        this.contextAttributes = newContextAttributes;
    }

    public void updateAuthorsNote(String authorsNote) {

        ContextAttributes newContextAttributes = this.contextAttributes.updateAuthorsNote(authorsNote);
        this.contextAttributes = newContextAttributes;
    }

    public void updateRemember(String remember) {

        ContextAttributes newContextAttributes = this.contextAttributes.updateRemember(remember);
        this.contextAttributes = newContextAttributes;
    }

    public static final class Builder {

        private String id;
        private String name;
        private String description;
        private String adventureStart;
        private String worldId;
        private String personaId;
        private String channelId;
        private boolean isMultiplayer;
        private GameMode gameMode;
        private ContextAttributes contextAttributes;
        private ModelConfiguration modelConfiguration;
        private Moderation moderation;
        private Visibility visibility;
        private Permissions permissions;
        private String creatorId;
        private OffsetDateTime creationDate;
        private OffsetDateTime lastUpdateDate;
        private int version;

        private Builder() {
        }

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder name(String name) {

            this.name = name;
            return this;
        }

        public Builder description(String description) {

            this.description = description;
            return this;
        }

        public Builder adventureStart(String adventureStart) {

            this.adventureStart = adventureStart;
            return this;
        }

        public Builder worldId(String worldId) {

            this.worldId = worldId;
            return this;
        }

        public Builder personaId(String personaId) {

            this.personaId = personaId;
            return this;
        }

        public Builder gameMode(GameMode gameMode) {

            this.gameMode = gameMode;
            return this;
        }

        public Builder isMultiplayer(boolean isMultiplayer) {

            this.isMultiplayer = isMultiplayer;
            return this;
        }

        public Builder channelId(String channelId) {

            this.channelId = channelId;
            return this;
        }

        public Builder modelConfiguration(ModelConfiguration modelConfiguration) {

            this.modelConfiguration = modelConfiguration;
            return this;
        }

        public Builder moderation(Moderation moderation) {

            this.moderation = moderation;
            return this;
        }

        public Builder contextAttributes(ContextAttributes contextAttributes) {

            this.contextAttributes = contextAttributes;
            return this;
        }

        public Builder visibility(Visibility visibility) {

            this.visibility = visibility;
            return this;
        }

        public Builder permissions(Permissions permissions) {

            this.permissions = permissions;
            return this;
        }

        public Builder creatorId(String creatorId) {

            this.creatorId = creatorId;
            return this;
        }

        public Builder creationDate(OffsetDateTime creationDate) {

            this.creationDate = creationDate;
            return this;
        }

        public Builder lastUpdateDate(OffsetDateTime lastUpdateDate) {

            this.lastUpdateDate = lastUpdateDate;
            return this;
        }

        public Builder version(int version) {

            this.version = version;
            return this;
        }

        public Adventure build() {

            if (isBlank(name)) {
                throw new BusinessRuleViolationException("Adventure name cannot be null or empty");
            }

            if (isBlank(channelId)) {
                throw new BusinessRuleViolationException("Channel ID cannot be null or empty");
            }

            if (modelConfiguration == null) {
                throw new BusinessRuleViolationException("Model configuration cannot be null");
            }

            if (moderation == null) {
                throw new BusinessRuleViolationException("Moderation cannot be null");
            }

            if (visibility == null) {
                throw new BusinessRuleViolationException("Visibility cannot be null");
            }

            if (permissions == null) {
                throw new BusinessRuleViolationException("Permissions cannot be null");
            }

            if (gameMode == null) {
                throw new BusinessRuleViolationException("Game Mode cannot be null");
            }

            return new Adventure(this);
        }
    }
}
