package me.moirai.storyengine.core.port.inbound.adventure;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class UpdateAdventureResult {

    private final OffsetDateTime lastUpdatedDateTime;

    @JsonCreator
    private UpdateAdventureResult(@JsonProperty("lastUpdatedDateTime") OffsetDateTime lastUpdatedDateTime) {
        this.lastUpdatedDateTime = lastUpdatedDateTime;
    }

    public static UpdateAdventureResult build(OffsetDateTime lastUpdatedDateTime) {

        return new UpdateAdventureResult(lastUpdatedDateTime);
    }

    public OffsetDateTime getLastUpdatedDateTime() {
        return lastUpdatedDateTime;
    }
}
