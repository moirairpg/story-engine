package me.moirai.storyengine.core.domain.adventure;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import me.moirai.storyengine.common.annotation.RandomUuid;
import me.moirai.storyengine.common.domain.Asset;

@Entity
@Table(name = "adventure_lorebook")
public class AdventureLorebookEntry extends Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @RandomUuid
    @Column(name = "public_id")
    private UUID publicId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "regex")
    private String regex;

    @Column(name = "player_id")
    private String playerId;

    @Column(name = "is_player_character")
    private boolean isPlayerCharacter;

    @Column(name = "adventure_id")
    private Long adventureId;

    private AdventureLorebookEntry(Builder builder) {

        super();
        this.name = builder.name;
        this.regex = builder.regex;
        this.description = builder.description;
        this.playerId = builder.playerId;
        this.isPlayerCharacter = builder.playerId != null;
    }

    protected AdventureLorebookEntry() {
        super();
    }

    public static Builder builder() {

        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
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

        private String name;
        private String regex;
        private String description;
        private String playerId;

        private Builder() {
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

        public AdventureLorebookEntry build() {

            return new AdventureLorebookEntry(this);
        }
    }
}
