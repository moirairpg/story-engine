package me.moirai.storyengine.infrastructure.inbound.rest.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePersonaResponse {

    private String id;

    public CreatePersonaResponse() {
    }

    private CreatePersonaResponse(String id) {
        this.id = id;
    }

    public static CreatePersonaResponse build(String id) {

        return new CreatePersonaResponse(id);
    }

    public String getId() {
        return id;
    }
}
