package me.moirai.storyengine.core.port.inbound.world;

import java.time.OffsetDateTime;

public final class UpdateWorldLorebookEntryResult {

    private final OffsetDateTime lastUpdatedDateTime;

    public UpdateWorldLorebookEntryResult(OffsetDateTime lastUpdatedDateTime) {
        this.lastUpdatedDateTime = lastUpdatedDateTime;
    }

    public static UpdateWorldLorebookEntryResult build(OffsetDateTime lastUpdatedDateTime) {

        return new UpdateWorldLorebookEntryResult(lastUpdatedDateTime);
    }

    public OffsetDateTime getLastUpdatedDateTime() {
        return lastUpdatedDateTime;
    }
}
