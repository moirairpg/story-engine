package me.moirai.storyengine.core.port.inbound.persona;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class CreatePersonaResult {

    private final String id;

    @JsonCreator
    private CreatePersonaResult(@JsonProperty("id") String id) {
        this.id = id;
    }

    public static CreatePersonaResult build(String id) {

        return new CreatePersonaResult(id);
    }

    public String getId() {
        return id;
    }
}
