package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("error")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public class CompletionResponseError {

    @JsonProperty("message")
    private String message;

    @JsonProperty("type")
    private String type;

    @JsonProperty("param")
    private String param;

    @JsonProperty("code")
    private String code;

    public CompletionResponseError() {
    }

    private CompletionResponseError(Builder builder) {
        this.message = builder.message;
        this.type = builder.type;
        this.param = builder.param;
        this.code = builder.code;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getParam() {
        return param;
    }

    public String getCode() {
        return code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "CompletionResponseError{" +
                "message='" + message + "\', " +
                "type='" + type + "\', " +
                "param='" + param + "\', " +
                "code='" + code + "\', " +
                '}';
    }

    public static final class Builder {

        private String message;
        private String type;
        private String param;
        private String code;

        private Builder() {
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder param(String param) {
            this.param = param;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public CompletionResponseError build() {
            return new CompletionResponseError(this);
        }
    }
}
