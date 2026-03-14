package me.moirai.storyengine.infrastructure.inbound.rest.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAdventureResponse {

    private String id;

    public CreateAdventureResponse() {
    }

    private CreateAdventureResponse(String id) {
        this.id = id;
    }

    public static CreateAdventureResponse build(String id) {

        return new CreateAdventureResponse(id);
    }

    public String getId() {
        return id;
    }
}
