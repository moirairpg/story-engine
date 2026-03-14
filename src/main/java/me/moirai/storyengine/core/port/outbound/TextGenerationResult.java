package me.moirai.storyengine.core.port.outbound;

public final class TextGenerationResult {

    private final String outputText;
    private final Integer promptTokens;
    private final Integer completionTokens;
    private final Integer totalTokens;

    public TextGenerationResult(Builder builder) {

        this.outputText = builder.outputText;
        this.promptTokens = builder.promptTokens;
        this.completionTokens = builder.completionTokens;
        this.totalTokens = builder.totalTokens;
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

    public static final class Builder {

        private String outputText;
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;

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

        public TextGenerationResult build() {
            return new TextGenerationResult(this);
        }
    }
}
