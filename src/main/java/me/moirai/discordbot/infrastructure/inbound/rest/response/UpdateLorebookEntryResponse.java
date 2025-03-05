package me.moirai.discordbot.infrastructure.inbound.rest.response;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateLorebookEntryResponse {

    private OffsetDateTime lastUpdateDate;

    public UpdateLorebookEntryResponse() {
    }

    private UpdateLorebookEntryResponse(OffsetDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public static UpdateLorebookEntryResponse build(OffsetDateTime lastUpdateDate) {

        return new UpdateLorebookEntryResponse(lastUpdateDate);
    }

    public OffsetDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }
}
