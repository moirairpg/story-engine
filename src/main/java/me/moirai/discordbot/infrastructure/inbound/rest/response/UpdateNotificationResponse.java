package me.moirai.discordbot.infrastructure.inbound.rest.response;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateNotificationResponse {

    private OffsetDateTime lastUpdateDate;

    public UpdateNotificationResponse() {
    }

    private UpdateNotificationResponse(OffsetDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public static UpdateNotificationResponse build(OffsetDateTime lastUpdateDate) {

        return new UpdateNotificationResponse(lastUpdateDate);
    }

    public OffsetDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }
}
