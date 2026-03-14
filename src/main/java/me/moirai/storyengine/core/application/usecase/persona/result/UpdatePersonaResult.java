package me.moirai.storyengine.core.application.usecase.persona.result;

import java.time.OffsetDateTime;

public final class UpdatePersonaResult {

    private final OffsetDateTime lastUpdatedDateTime;

    private UpdatePersonaResult(OffsetDateTime lastUpdatedDateTime) {
        this.lastUpdatedDateTime = lastUpdatedDateTime;
    }

    public static UpdatePersonaResult build(OffsetDateTime lastUpdatedDateTime) {

        return new UpdatePersonaResult(lastUpdatedDateTime);
    }

    public OffsetDateTime getLastUpdatedDateTime() {
        return lastUpdatedDateTime;
    }
}
