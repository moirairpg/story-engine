package me.moirai.storyengine.core.domain.world;

import java.time.OffsetDateTime;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import me.moirai.storyengine.common.annotation.UuidIdentifier;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.core.domain.Permissions;
import me.moirai.storyengine.core.domain.ShareableAsset;
import me.moirai.storyengine.core.domain.Visibility;

@Entity(name = "World")
@Table(name = "world")
public class World extends ShareableAsset {

    @Id
    @UuidIdentifier
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "adventure_start", nullable = false)
    private String adventureStart;

    private World(Builder builder) {

        super(builder.creatorId, builder.creationDate,
                builder.lastUpdateDate, builder.permissions, builder.visibility, builder.version);

        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.adventureStart = builder.adventureStart;
    }

    protected World() {
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

    public String getDescription() {
        return description;
    }

    public String getAdventureStart() {
        return adventureStart;
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

    public static final class Builder {

        private String id;
        private String name;
        private String description;
        private String adventureStart;
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

        public World build() {

            if (StringUtils.isBlank(name)) {
                throw new BusinessRuleViolationException("Persona name cannot be null or empty");
            }

            if (visibility == null) {
                throw new BusinessRuleViolationException("Visibility cannot be null");
            }

            if (permissions == null) {
                throw new BusinessRuleViolationException("Permissions cannot be null");
            }

            return new World(this);
        }
    }
}
