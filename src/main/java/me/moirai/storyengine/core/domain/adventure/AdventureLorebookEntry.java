package me.moirai.storyengine.core.domain.adventure;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import me.moirai.storyengine.common.annotation.UuidIdentifier;
import me.moirai.storyengine.common.domain.Asset;

@Entity
@Table(name = "adventure_lorebook")
public class AdventureLorebookEntry extends Asset {

    @Id
    @UuidIdentifier
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "regex", nullable = false)
    private String regex;

    @Column(name = "player_id")
    private String playerId;

    @Column(name = "is_player_character", nullable = false)
    private boolean isPlayerCharacter;

    @Column(name = "adventure_id", insertable = false, updatable = false)
    private String adventureId;

    private AdventureLorebookEntry(Builder builder) {

        super(builder.creatorId, builder.creationDate, builder.lastUpdateDate, builder.version);
        this.id = builder.id;
        this.name = builder.name;
        this.regex = builder.regex;
        this.description = builder.description;
        this.playerId = builder.playerId;
        this.isPlayerCharacter = builder.isPlayerCharacter;
    }

    protected AdventureLorebookEntry() {
        super();
    }

    public static Builder builder() {

        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRegex() {
        return regex;
    }

    public String getDescription() {
        return description;
    }

    public String getPlayerId() {
        return playerId;
    }

    public boolean isPlayerCharacter() {
        return isPlayerCharacter;
    }

    public void updateName(String name) {

        this.name = name;
    }

    public void updateDescription(String description) {

        this.description = description;
    }

    public void updateRegex(String regex) {

        this.regex = regex;
    }

    public void assignPlayer(String playerId) {

        this.isPlayerCharacter = true;
        this.playerId = playerId;
    }

    public void unassignPlayer() {

        this.isPlayerCharacter = false;
        this.playerId = null;
    }

    public static final class Builder {

        private String id;
        private String name;
        private String regex;
        private String description;
        private String playerId;
        private String creatorId;
        private boolean isPlayerCharacter;
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

        public Builder regex(String regex) {

            this.regex = regex;
            return this;
        }

        public Builder playerId(String playerId) {

            this.playerId = playerId;
            return this;
        }

        public Builder isPlayerCharacter(boolean isPlayerCharacter) {

            this.isPlayerCharacter = isPlayerCharacter;
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

        public AdventureLorebookEntry build() {

            return new AdventureLorebookEntry(this);
        }
    }
}
