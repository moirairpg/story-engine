package me.moirai.storyengine.core.port.inbound.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = AiModelResult.Builder.class)
public class AiModelResult {

    private final String fullModelName;
    private final String internalModelName;
    private final String officialModelName;
    private final int hardTokenLimit;

    private AiModelResult(Builder builder) {
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

    @JsonPOJOBuilder(withPrefix = "")
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

        public AiModelResult build() {
            return new AiModelResult(this);
        }
    }
}
