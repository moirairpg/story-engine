package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModerationRequest {

    @JsonProperty("input")
    private String input;

    public ModerationRequest() {
    }

    private ModerationRequest(String input) {
        this.input = input;
    }

    public static ModerationRequest build(String input) {

        return new ModerationRequest(input);
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }
}