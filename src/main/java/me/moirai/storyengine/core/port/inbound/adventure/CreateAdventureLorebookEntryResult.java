package me.moirai.storyengine.core.port.inbound.adventure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class CreateAdventureLorebookEntryResult {

    private final String id;

    @JsonCreator
    public CreateAdventureLorebookEntryResult(@JsonProperty("id") String id) {
        this.id = id;
    }

    public static CreateAdventureLorebookEntryResult build(String id) {

        return new CreateAdventureLorebookEntryResult(id);
    }

    public String getId() {
        return id;
    }
}
