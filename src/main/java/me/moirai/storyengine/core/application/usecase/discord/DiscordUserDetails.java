package me.moirai.storyengine.core.application.usecase.discord;

public final class DiscordUserDetails {

    private final String id;
    private final String username;
    private final String nickname;
    private final String mention;
    private final String avatarUrl;

    public DiscordUserDetails(Builder builder) {

        this.id = builder.id;
        this.username = builder.username;
        this.nickname = builder.nickname;
        this.mention = builder.mention;
        this.avatarUrl = builder.avatarUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getMention() {
        return mention;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public static final class Builder {

        private String id;
        private String username;
        private String nickname;
        private String mention;
        private String avatarUrl;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
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

        public Builder mention(String mention) {
            this.mention = mention;
            return this;
        }

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public DiscordUserDetails build() {
            return new DiscordUserDetails(this);
        }
    }
}
