package me.moirai.storyengine.core.application.usecase.adventure.result;

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
