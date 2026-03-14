package me.moirai.storyengine.core.application.usecase.persona.result;

public final class CreatePersonaResult {

    private final String id;

    private CreatePersonaResult(String id) {
        this.id = id;
    }

    public static CreatePersonaResult build(String id) {

        return new CreatePersonaResult(id);
    }

    public String getId() {
        return id;
    }
}
