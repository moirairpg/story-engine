package me.moirai.storyengine.core.port.inbound.adventure;

import java.time.OffsetDateTime;

public final class UpdateAdventureResult {

    private final OffsetDateTime lastUpdatedDateTime;

    private UpdateAdventureResult(OffsetDateTime lastUpdatedDateTime) {
        this.lastUpdatedDateTime = lastUpdatedDateTime;
    }

    public static UpdateAdventureResult build(OffsetDateTime lastUpdatedDateTime) {

        return new UpdateAdventureResult(lastUpdatedDateTime);
    }

    public OffsetDateTime getLastUpdatedDateTime() {
        return lastUpdatedDateTime;
    }
}
