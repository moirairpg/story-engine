package me.moirai.storyengine.core.port.inbound.world;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class CreateWorldResult {

    private final String id;

    @JsonCreator
    public CreateWorldResult(@JsonProperty("id") String id) {
        this.id = id;
    }

    public static CreateWorldResult build(String id) {

        return new CreateWorldResult(id);
    }

    public String getId() {
        return id;
    }
}
