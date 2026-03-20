package me.moirai.storyengine.core.domain.world;

import java.time.OffsetDateTime;
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
@Table(name = "world_lorebook")
public class WorldLorebookEntry extends Asset {

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

    @Column(name = "world_id")
    private Long worldId;

    private WorldLorebookEntry(Builder builder) {

        super(builder.creatorId, builder.creationDate, builder.lastUpdateDate);

        this.name = builder.name;
        this.regex = builder.regex;
        this.description = builder.description;
    }

    protected WorldLorebookEntry() {
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

    public void updateName(String name) {

        this.name = name;
    }

    public void updateDescription(String description) {

        this.description = description;
    }

    public void updateRegex(String regex) {

        this.regex = regex;
    }

    public static final class Builder {

        private String name;
        private String regex;
        private String description;
        private String creatorId;
        private OffsetDateTime creationDate;
        private OffsetDateTime lastUpdateDate;

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

        public WorldLorebookEntry build() {

            return new WorldLorebookEntry(this);
        }
    }
}
