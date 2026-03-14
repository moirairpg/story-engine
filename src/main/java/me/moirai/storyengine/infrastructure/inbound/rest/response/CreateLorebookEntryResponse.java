package me.moirai.storyengine.infrastructure.inbound.rest.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateLorebookEntryResponse {

    private String id;

    public CreateLorebookEntryResponse() {
    }

    private CreateLorebookEntryResponse(String id) {
        this.id = id;
    }

    public static CreateLorebookEntryResponse build(String id) {

        return new CreateLorebookEntryResponse(id);
    }

    public String getId() {
        return id;
    }
}
