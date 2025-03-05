package me.moirai.discordbot.infrastructure.inbound.rest.response;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePersonaResponse {

    private OffsetDateTime lastUpdateDate;

    public UpdatePersonaResponse() {
    }

    private UpdatePersonaResponse(OffsetDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public static UpdatePersonaResponse build(OffsetDateTime lastUpdateDate) {

        return new UpdatePersonaResponse(lastUpdateDate);
    }

    public OffsetDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }
}
