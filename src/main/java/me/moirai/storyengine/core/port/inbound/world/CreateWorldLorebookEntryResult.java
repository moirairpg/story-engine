package me.moirai.storyengine.core.port.inbound.world;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class CreateWorldLorebookEntryResult {

    private final String id;

    @JsonCreator
    public CreateWorldLorebookEntryResult(@JsonProperty("id") String id) {
        this.id = id;
    }

    public static CreateWorldLorebookEntryResult build(String id) {

        return new CreateWorldLorebookEntryResult(id);
    }

    public String getId() {
        return id;
    }
}
