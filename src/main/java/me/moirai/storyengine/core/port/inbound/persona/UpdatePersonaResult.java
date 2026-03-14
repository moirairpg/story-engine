package me.moirai.storyengine.core.port.inbound.persona;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class UpdatePersonaResult {

    private final OffsetDateTime lastUpdatedDateTime;

    @JsonCreator
    private UpdatePersonaResult(@JsonProperty("lastUpdatedDateTime") OffsetDateTime lastUpdatedDateTime) {
        this.lastUpdatedDateTime = lastUpdatedDateTime;
    }

    public static UpdatePersonaResult build(OffsetDateTime lastUpdatedDateTime) {

        return new UpdatePersonaResult(lastUpdatedDateTime);
    }

    public OffsetDateTime getLastUpdatedDateTime() {
        return lastUpdatedDateTime;
    }
}
