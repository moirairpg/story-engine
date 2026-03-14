package me.moirai.storyengine.core.application.usecase.adventure.result;

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
