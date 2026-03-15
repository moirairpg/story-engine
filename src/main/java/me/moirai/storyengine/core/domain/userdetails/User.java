package me.moirai.storyengine.core.domain.userdetails;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import me.moirai.storyengine.common.domain.Asset;

@Entity
@Table(name = "moirai_user")
public class User extends Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private String publicId;

    @Column(name = "discord_id", unique = true, nullable = false)
    private String discordId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    public User(Builder builder) {

        super(builder.creatorId, builder.creationDate, builder.lastUpdateDate, builder.version);

        this.discordId = builder.discordId;
        this.role = builder.role;
    }

    protected User() {
        super();
    }

    @PrePersist
    private void generatePublicId() {
        if (publicId == null) {
            publicId = UUID.randomUUID().toString();
        }
    }

    public static Builder builder() {

        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public String getPublicId() {
        return publicId;
    }

    public String getDiscordId() {
        return discordId;
    }

    public Role getRole() {
        return role;
    }

    public static final class Builder {

        private String discordId;
        private Role role;
        private String creatorId;
        private OffsetDateTime creationDate;
        private OffsetDateTime lastUpdateDate;
        private int version;

        private Builder() {
        }

        public Builder discordId(String discordId) {

            this.discordId = discordId;
            return this;
        }

        public Builder role(Role role) {

            this.role = role;
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

        public User build() {
            return new User(this);
        }
    }
}
