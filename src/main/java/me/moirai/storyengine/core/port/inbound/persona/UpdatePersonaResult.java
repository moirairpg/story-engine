package me.moirai.storyengine.core.port.inbound.persona;

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
