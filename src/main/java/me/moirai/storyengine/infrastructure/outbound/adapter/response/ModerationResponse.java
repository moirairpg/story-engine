package me.moirai.storyengine.infrastructure.outbound.adapter.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModerationResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("model")
    private String model;

    @JsonProperty("results")
    private List<ModerationResult> results;

    public ModerationResponse() {
    }

    private ModerationResponse(Builder builder) {

        this.id = builder.id;
        this.model = builder.model;

        this.results = Collections
                .unmodifiableList(builder.results != null ? new ArrayList<>(builder.results) : Collections.emptyList());
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public List<ModerationResult> getResults() {
        return results;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setResults(List<ModerationResult> results) {
        this.results = results;
    }

    public static final class Builder {

        private String id;
        private String model;
        private List<ModerationResult> results;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder results(List<ModerationResult> results) {
            this.results = results;
            return this;
        }

        public ModerationResponse build() {
            return new ModerationResponse(this);
        }
    }
}