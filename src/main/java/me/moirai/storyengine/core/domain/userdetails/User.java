package me.moirai.storyengine.core.domain.userdetails;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import me.moirai.storyengine.common.annotation.RandomUuid;
import me.moirai.storyengine.common.domain.Asset;
import me.moirai.storyengine.common.enums.Role;

@Entity
@Table(name = "moirai_user")
public class User extends Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @RandomUuid
    @Column(name = "public_id")
    private UUID publicId;

    @Column(name = "discord_id")
    private String discordId;

    @Column(name = "username")
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    public User(Builder builder) {

        this.discordId = builder.discordId;
        this.username = builder.username;
        this.role = builder.role;
    }

    protected User() {
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

    public String getDiscordId() {
        return discordId;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public static final class Builder {

        private String discordId;
        private String username;
        private Role role;

        private Builder() {
        }

        public Builder discordId(String discordId) {

            this.discordId = discordId;
            return this;
        }

        public Builder username(String username) {

            this.username = username;
            return this;
        }

        public Builder role(Role role) {

            this.role = role;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
