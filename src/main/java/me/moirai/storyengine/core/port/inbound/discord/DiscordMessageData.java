package me.moirai.storyengine.core.port.inbound.discord;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

public final class DiscordMessageData {

    private final String id;
    private final String channelId;
    private final String content;
    private final DiscordUserDetails author;
    private final List<DiscordUserDetails> mentionedUsers;

    public DiscordMessageData(Builder builder) {

        this.id = builder.id;
        this.channelId = builder.channelId;
        this.content = builder.content;
        this.author = builder.author;
        this.mentionedUsers = unmodifiableList(builder.mentionedUsers);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getChannelId() {
        return channelId;
    }

    public DiscordUserDetails getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public List<DiscordUserDetails> getMentionedUsers() {
        return mentionedUsers;
    }

    public static final class Builder {

        private String id;
        private String channelId;
        private String content;
        private DiscordUserDetails author;
        private List<DiscordUserDetails> mentionedUsers = new ArrayList<>();

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder author(DiscordUserDetails author) {
            this.author = author;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder mentionedUsers(List<DiscordUserDetails> mentionedUsers) {

            if (mentionedUsers != null) {
                this.mentionedUsers.addAll(mentionedUsers);
            }

            return this;
        }

        public DiscordMessageData build() {
            return new DiscordMessageData(this);
        }
    }
}
