package me.moirai.storyengine.core.port.inbound.adventure;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class UpdateAdventureLorebookEntryResult {

    private final OffsetDateTime lastUpdatedDateTime;

    @JsonCreator
    public UpdateAdventureLorebookEntryResult(@JsonProperty("lastUpdatedDateTime") OffsetDateTime lastUpdatedDateTime) {
        this.lastUpdatedDateTime = lastUpdatedDateTime;
    }

    public static UpdateAdventureLorebookEntryResult build(OffsetDateTime lastUpdatedDateTime) {

        return new UpdateAdventureLorebookEntryResult(lastUpdatedDateTime);
    }

    public OffsetDateTime getLastUpdatedDateTime() {
        return lastUpdatedDateTime;
    }
}
