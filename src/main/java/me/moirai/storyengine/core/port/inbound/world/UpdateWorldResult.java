package me.moirai.storyengine.core.port.inbound.world;

import java.time.OffsetDateTime;

public final class UpdateWorldResult {

    private final OffsetDateTime lastUpdatedDateTime;

    public UpdateWorldResult(OffsetDateTime lastUpdatedDateTime) {
        this.lastUpdatedDateTime = lastUpdatedDateTime;
    }

    public static UpdateWorldResult build(OffsetDateTime lastUpdatedDateTime) {

        return new UpdateWorldResult(lastUpdatedDateTime);
    }

    public OffsetDateTime getLastUpdatedDateTime() {
        return lastUpdatedDateTime;
    }
}
