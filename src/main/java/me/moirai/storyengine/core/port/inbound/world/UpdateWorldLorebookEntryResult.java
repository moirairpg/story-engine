package me.moirai.storyengine.core.port.inbound.world;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class UpdateWorldLorebookEntryResult {

    private final OffsetDateTime lastUpdatedDateTime;

    @JsonCreator
    public UpdateWorldLorebookEntryResult(@JsonProperty("lastUpdatedDateTime") OffsetDateTime lastUpdatedDateTime) {
        this.lastUpdatedDateTime = lastUpdatedDateTime;
    }

    public static UpdateWorldLorebookEntryResult build(OffsetDateTime lastUpdatedDateTime) {

        return new UpdateWorldLorebookEntryResult(lastUpdatedDateTime);
    }

    public OffsetDateTime getLastUpdatedDateTime() {
        return lastUpdatedDateTime;
    }
}
