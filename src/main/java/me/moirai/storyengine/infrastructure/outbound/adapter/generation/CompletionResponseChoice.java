package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import me.moirai.storyengine.core.port.outbound.generation.ChatMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompletionResponseChoice {

    @JsonProperty("text")
    private String text;

    @JsonProperty("finish_reason")
    private String finishReason;

    @JsonProperty("index")
    private int index;

    @JsonProperty("message")
    private ChatMessage message;

    public CompletionResponseChoice() {
    }

    private CompletionResponseChoice(Builder builder) {
        this.text = builder.text;
        this.finishReason = builder.finishReason;
        this.index = builder.index;
        this.message = builder.message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getText() {
        return text;
    }

    public String getFinishReason() {
        return finishReason;
    }

    public int getIndex() {
        return index;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    public static final class Builder {

        private String text;
        private String finishReason;
        private int index;
        private ChatMessage message;

        private Builder() {
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder finishReason(String finishReason) {
            this.finishReason = finishReason;
            return this;
        }

        public Builder index(int index) {
            this.index = index;
            return this;
        }

        public Builder message(ChatMessage message) {
            this.message = message;
            return this;
        }

        public CompletionResponseChoice build() {
            return new CompletionResponseChoice(this);
        }
    }
}