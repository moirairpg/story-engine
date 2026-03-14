package me.moirai.storyengine.core.application.model.request;

public final class TextModerationRequest {

    private final String input;

    public TextModerationRequest(String input) {
        this.input = input;
    }

    public static TextModerationRequest build(String input) {
        return new TextModerationRequest(input);
    }

    public String getInput() {
        return input;
    }
}
