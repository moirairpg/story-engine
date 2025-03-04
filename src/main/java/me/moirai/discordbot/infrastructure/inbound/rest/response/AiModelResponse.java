package me.moirai.discordbot.infrastructure.inbound.rest.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiModelResponse {

    private String fullModelName;
    private String internalModelName;
    private String officialModelName;
    private int hardTokenLimit;

    public AiModelResponse() {
    }

    private AiModelResponse(Builder builder) {
        this.fullModelName = builder.fullModelName;
        this.internalModelName = builder.internalModelName;
        this.officialModelName = builder.officialModelName;
        this.hardTokenLimit = builder.hardTokenLimit;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getFullModelName() {
        return fullModelName;
    }

    public String getInternalModelName() {
        return internalModelName;
    }

    public String getOfficialModelName() {
        return officialModelName;
    }

    public int getHardTokenLimit() {
        return hardTokenLimit;
    }

    public static final class Builder {

        private String fullModelName;
        private String internalModelName;
        private String officialModelName;
        private int hardTokenLimit;

        public Builder fullModelName(String fullModelName) {
            this.fullModelName = fullModelName;
            return this;
        }

        public Builder internalModelName(String internalModelName) {
            this.internalModelName = internalModelName;
            return this;
        }

        public Builder officialModelName(String officialModelName) {
            this.officialModelName = officialModelName;
            return this;
        }

        public Builder hardTokenLimit(int hardTokenLimit) {
            this.hardTokenLimit = hardTokenLimit;
            return this;
        }

        public AiModelResponse build() {
            return new AiModelResponse(this);
        }
    }
}
