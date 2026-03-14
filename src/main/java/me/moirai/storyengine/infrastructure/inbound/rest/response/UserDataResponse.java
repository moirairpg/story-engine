package me.moirai.storyengine.infrastructure.inbound.rest.response;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDataResponse {

    private String discordId;
    private String username;
    private String nickname;
    private String avatar;
    private OffsetDateTime joinDate;

    public UserDataResponse() {
    }

    private UserDataResponse(Builder builder) {
        this.discordId = builder.discordId;
        this.username = builder.username;
        this.nickname = builder.nickname;
        this.avatar = builder.avatar;
        this.joinDate = builder.joinDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getDiscordId() {
        return discordId;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public OffsetDateTime getJoinDate() {
        return joinDate;
    }

    public static final class Builder {
        private String discordId;
        private String username;
        private String nickname;
        private String avatar;
        private OffsetDateTime joinDate;

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

        public Builder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public Builder joinDate(OffsetDateTime joinDate) {
            this.joinDate = joinDate;
            return this;
        }

        public UserDataResponse build() {
            return new UserDataResponse(this);
        }
    }
}