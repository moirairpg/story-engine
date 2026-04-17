package me.moirai.storyengine.infrastructure.inbound.rest.response;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {

    private HttpStatus code;
    private String message;
    private List<String> details;

    public ErrorResponse() {
    }

    private ErrorResponse(Builder builder) {
        this.code = builder.code;
        this.message = builder.message;
        this.details = builder.details;
    }

    public static Builder builder() {
        return new Builder();
    }

    public HttpStatus getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "code='" + code + "\', " +
                "message='" + message + "\', " +
                "details=" + (details != null ? '\'' + String.join(", ", details) + '\'' : "null") +
                '}';
    }

    public static final class Builder {
        private HttpStatus code;
        private String message;
        private List<String> details;

        private Builder() {
        }

        public Builder code(HttpStatus code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder details(List<String> details) {
            this.details = emptyIfNull(details);
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}