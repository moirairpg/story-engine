package me.moirai.storyengine.core.port.inbound.adventure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class CreateAdventureResult {

    private final String id;

    @JsonCreator
    private CreateAdventureResult(@JsonProperty("id") String id) {
        this.id = id;
    }

    public static CreateAdventureResult build(String id) {

        return new CreateAdventureResult(id);
    }

    public String getId() {
        return id;
    }
}
