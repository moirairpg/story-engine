package me.moirai.storyengine.core.port.inbound.completion;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = CompleteTextResult.Builder.class)
public class CompleteTextResult {

    private final String outputText;
    private final Integer promptTokens;
    private final Integer completionTokens;
    private final Integer totalTokens;
    private final int[] tokenIds;
    private final String[] tokens;

    public CompleteTextResult(Builder builder) {

        this.outputText = builder.outputText;
        this.promptTokens = builder.promptTokens;
        this.completionTokens = builder.completionTokens;
        this.totalTokens = builder.totalTokens;
        this.tokenIds = builder.tokenIds;
        this.tokens = builder.tokens;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getOutputText() {
        return outputText;
    }

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public Integer getCompletionTokens() {
        return completionTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public int[] getTokenIds() {
        return tokenIds;
    }

    public String[] getTokens() {
        return tokens;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {

        private String outputText;
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
        private int[] tokenIds;
        private String[] tokens;

        private Builder() {
        }

        public Builder outputText(String outputText) {
            this.outputText = outputText;
            return this;
        }

        public Builder promptTokens(Integer promptTokens) {
            this.promptTokens = promptTokens;
            return this;
        }

        public Builder completionTokens(Integer completionTokens) {
            this.completionTokens = completionTokens;
            return this;
        }

        public Builder totalTokens(Integer totalTokens) {
            this.totalTokens = totalTokens;
            return this;
        }

        public Builder tokenIds(int[] tokenIds) {
            this.tokenIds = tokenIds;
            return this;
        }

        public Builder tokens(String[] tokens) {
            this.tokens = tokens;
            return this;
        }

        public CompleteTextResult build() {
            return new CompleteTextResult(this);
        }
    }
}
