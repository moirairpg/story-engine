package me.moirai.storyengine.core.port.inbound.world;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class UpdateWorldResult {

    private final OffsetDateTime lastUpdatedDateTime;

    @JsonCreator
    public UpdateWorldResult(@JsonProperty("lastUpdatedDateTime") OffsetDateTime lastUpdatedDateTime) {
        this.lastUpdatedDateTime = lastUpdatedDateTime;
    }

    public static UpdateWorldResult build(OffsetDateTime lastUpdatedDateTime) {

        return new UpdateWorldResult(lastUpdatedDateTime);
    }

    public OffsetDateTime getLastUpdatedDateTime() {
        return lastUpdatedDateTime;
    }
}
