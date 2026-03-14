package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompletionResponse {

    @JsonProperty("choices")
    private List<CompletionResponseChoice> choices;

    @JsonProperty("usage")
    private CompletionResponseUsage usage;

    public CompletionResponse() {
    }

    private CompletionResponse(Builder builder) {

        this.usage = builder.usage;

        this.choices = Collections
                .unmodifiableList(builder.choices != null ? new ArrayList<>(builder.choices) : Collections.emptyList());
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<CompletionResponseChoice> getChoices() {
        return choices;
    }

    public CompletionResponseUsage getUsage() {
        return usage;
    }

    public void setChoices(List<CompletionResponseChoice> choices) {
        this.choices = choices;
    }

    public void setUsage(CompletionResponseUsage usage) {
        this.usage = usage;
    }

    public static final class Builder {

        private List<CompletionResponseChoice> choices;
        private CompletionResponseUsage usage;

        private Builder() {
        }

        public Builder choices(List<CompletionResponseChoice> choices) {
            this.choices = choices;
            return this;
        }

        public Builder usage(CompletionResponseUsage usage) {
            this.usage = usage;
            return this;
        }

        public CompletionResponse build() {
            return new CompletionResponse(this);
        }
    }
}
