package me.moirai.storyengine.infrastructure.inbound.rest.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateWorldResponse {

    private String id;

    public CreateWorldResponse() {
    }

    public CreateWorldResponse(String id) {
        this.id = id;
    }

    public static CreateWorldResponse build(String id) {

        return new CreateWorldResponse(id);
    }

    public String getId() {
        return id;
    }
}
