package me.moirai.storyengine.core.port.inbound.adventure;

import java.time.OffsetDateTime;

public final class UpdateAdventureLorebookEntryResult {

    private final OffsetDateTime lastUpdatedDateTime;

    public UpdateAdventureLorebookEntryResult(OffsetDateTime lastUpdatedDateTime) {
        this.lastUpdatedDateTime = lastUpdatedDateTime;
    }

    public static UpdateAdventureLorebookEntryResult build(OffsetDateTime lastUpdatedDateTime) {

        return new UpdateAdventureLorebookEntryResult(lastUpdatedDateTime);
    }

    public OffsetDateTime getLastUpdatedDateTime() {
        return lastUpdatedDateTime;
    }
}
