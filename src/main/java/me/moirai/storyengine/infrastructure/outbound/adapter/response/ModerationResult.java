package me.moirai.storyengine.infrastructure.outbound.adapter.response;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModerationResult {

    @JsonProperty("categories")
    private Map<String, Boolean> categories;

    @JsonProperty("category_scores")
    private Map<String, String> categoryScores;

    @JsonProperty("flagged")
    private Boolean flagged;

    public ModerationResult() {
    }

    private ModerationResult(Builder builder) {

        this.flagged = builder.flagged;

        this.categories = Collections
                .unmodifiableMap(
                        builder.categories != null ? new HashMap<>(builder.categories) : Collections.emptyMap());

        this.categoryScores = Collections
                .unmodifiableMap(
                        builder.categoryScores != null ? new HashMap<>(builder.categoryScores)
                                : Collections.emptyMap());
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, Boolean> getCategories() {
        return categories;
    }

    public Map<String, String> getCategoryScores() {
        return categoryScores;
    }

    public Boolean getFlagged() {
        return flagged;
    }

    public void setCategories(Map<String, Boolean> categories) {
        this.categories = categories;
    }

    public void setCategoryScores(Map<String, String> categoryScores) {
        this.categoryScores = categoryScores;
    }

    public void setFlagged(Boolean flagged) {
        this.flagged = flagged;
    }

    public static final class Builder {

        private Map<String, Boolean> categories;
        private Map<String, String> categoryScores;
        private Boolean flagged;

        private Builder() {
        }

        public Builder categories(Map<String, Boolean> categories) {
            this.categories = categories;
            return this;
        }

        public Builder categoryScores(Map<String, String> categoryScores) {
            this.categoryScores = categoryScores;
            return this;
        }

        public Builder flagged(Boolean flagged) {
            this.flagged = flagged;
            return this;
        }

        public ModerationResult build() {
            return new ModerationResult(this);
        }
    }
}