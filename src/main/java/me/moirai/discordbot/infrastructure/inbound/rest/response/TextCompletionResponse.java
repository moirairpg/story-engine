package me.moirai.discordbot.infrastructure.inbound.rest.response;

import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TextCompletionResponse {

    private String outputText;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private long[] tokenIds;
    private String[] tokens;

    public TextCompletionResponse() {
    }

    public TextCompletionResponse(Builder builder) {

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

    public long[] getTokenIds() {
        return tokenIds;
    }

    public String[] getTokens() {
        return tokens;
    }

    public void setOutputText(String outputText) {
        this.outputText = outputText;
    }

    public void setPromptTokens(Integer promptTokens) {
        this.promptTokens = promptTokens;
    }

    public void setCompletionTokens(Integer completionTokens) {
        this.completionTokens = completionTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }

    public void setTokenIds(long[] tokenIds) {
        this.tokenIds = tokenIds;
    }

    public void setTokens(String[] tokens) {
        this.tokens = tokens;
    }

    public static final class Builder {

        private String outputText;
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
        private long[] tokenIds;
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

        public Builder tokenIds(long[] tokenIds) {
            this.tokenIds = nullToEmpty(tokenIds);
            return this;
        }

        public Builder tokens(String[] tokens) {
            this.tokens = nullToEmpty(tokens);
            return this;
        }

        public TextCompletionResponse build() {
            return new TextCompletionResponse(this);
        }
    }
}
