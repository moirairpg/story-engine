package me.moirai.storyengine.infrastructure.outbound.adapter.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {

    @JsonProperty("role")
    private String role;

    @JsonProperty("content")
    private String content;

    public ChatMessage() {
    }

    public ChatMessage(Builder builder) {
        this.role = builder.role;
        this.content = builder.content;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static final class Builder {

        private String role;
        private String content;

        private Builder() {
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public ChatMessage build() {
            return new ChatMessage(this);
        }
    }
}