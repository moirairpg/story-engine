package me.moirai.storyengine.core.domain.character;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.UUID;

import com.fasterxml.uuid.Generators;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import me.moirai.storyengine.common.domain.Asset;
import me.moirai.storyengine.common.enums.CharacterClass;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@Entity
@Table(name = "player_character")
public class PlayerCharacter extends Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id")
    private UUID publicId;

    @Column(name = "name")
    private String name;

    @Column(name = "player_id")
    private Long playerId;

    @Column(name = "personality")
    private String personality;

    @Column(name = "physical_description")
    private String physicalDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "character_class")
    private CharacterClass characterClass;

    @Column(name = "image_key")
    private String imageKey;

    protected PlayerCharacter() {
        super();
    }

    private PlayerCharacter(Builder builder) {

        super();

        this.publicId = Generators.timeBasedEpochGenerator().generate();
        this.name = builder.name;
        this.playerId = builder.playerId;
        this.personality = builder.personality;
        this.physicalDescription = builder.physicalDescription;
        this.characterClass = builder.characterClass;
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

    public Long getPlayerId() {
        return playerId;
    }

    public String getPersonality() {
        return personality;
    }

    public String getPhysicalDescription() {
        return physicalDescription;
    }

    public CharacterClass getCharacterClass() {
        return characterClass;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void updateName(String name) {

        if (name == null) {
            throw new BusinessRuleViolationException("Character must have a name");
        }

        this.name = name;
    }

    public void updatePersonality(String personality) {

        if (personality == null) {
            throw new BusinessRuleViolationException("Character must have a personality");
        }

        this.personality = personality;
    }

    public void updatePhysicalDescription(String physicalDescription) {

        if (physicalDescription == null) {
            throw new BusinessRuleViolationException("Character must have a physical description");
        }

        this.physicalDescription = physicalDescription;
    }

    public void updateCharacterClass(CharacterClass characterClass) {

        if (characterClass == null) {
            throw new BusinessRuleViolationException("Character must have a class");
        }

        this.characterClass = characterClass;
    }

    public void updateImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public String generateImageKey() {

        var imageId = Generators.timeBasedEpochGenerator().generate();
        return "characters/" + this.publicId + "/" + imageId + ".png";
    }

    public static final class Builder {

        private String name;
        private Long playerId;
        private String personality;
        private String physicalDescription;
        private CharacterClass characterClass;

        private Builder() {
        }

        public Builder name(String name) {

            if (isBlank(name)) {
                throw new BusinessRuleViolationException("Character name cannot be null or empty");
            }

            this.name = name;
            return this;
        }

        public Builder playerId(Long playerId) {

            if (playerId == null) {
                throw new BusinessRuleViolationException("Character must have an owner");
            }

            this.playerId = playerId;
            return this;
        }

        public Builder personality(String personality) {

            if (isBlank(personality)) {
                throw new BusinessRuleViolationException("Character personality cannot be null or empty");
            }

            this.personality = personality;
            return this;
        }

        public Builder physicalDescription(String physicalDescription) {

            if (isBlank(physicalDescription)) {
                throw new BusinessRuleViolationException("Character physical description cannot be null or empty");
            }

            this.physicalDescription = physicalDescription;
            return this;
        }

        public Builder characterClass(CharacterClass characterClass) {

            if (characterClass == null) {
                throw new BusinessRuleViolationException("Character class cannot be null");
            }

            this.characterClass = characterClass;
            return this;
        }

        public PlayerCharacter build() {

            return new PlayerCharacter(this);
        }
    }
}