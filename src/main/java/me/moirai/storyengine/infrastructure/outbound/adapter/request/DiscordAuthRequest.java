package me.moirai.storyengine.infrastructure.outbound.adapter.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = DiscordAuthRequest.Builder.class)
public class DiscordAuthRequest {

    @JsonProperty("client_id")
    private final String clientId;

    @JsonProperty("client_secret")
    private final String clientSecret;

    @JsonProperty("grant_type")
    private final String grantType;

    @JsonProperty("redirect_uri")
    private final String redirectUri;

    @JsonProperty("scope")
    private final String scope;

    @JsonProperty("code")
    private final String code;

    private DiscordAuthRequest(Builder builder) {
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        this.grantType = builder.grantType;
        this.redirectUri = builder.redirectUri;
        this.scope = builder.scope;
        this.code = builder.code;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getGrantType() {
        return grantType;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getScope() {
        return scope;
    }

    public String getCode() {
        return code;
    }

    @JsonPOJOBuilder
    public static final class Builder {

        private String clientId;
        private String clientSecret;
        private String grantType;
        private String redirectUri;
        private String scope;
        private String code;

        private Builder() {
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder grantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public DiscordAuthRequest build() {
            return new DiscordAuthRequest(this);
        }
    }
}
