package me.moirai.discordbot.infrastructure.inbound.rest.response;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateAdventureResponse {

    private OffsetDateTime lastUpdateDate;

    public UpdateAdventureResponse() {
    }

    private UpdateAdventureResponse(OffsetDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public static UpdateAdventureResponse build(OffsetDateTime lastUpdateDate) {

        return new UpdateAdventureResponse(lastUpdateDate);
    }

    public OffsetDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }
}
