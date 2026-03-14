package me.moirai.storyengine.core.application.usecase.world.result;

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
