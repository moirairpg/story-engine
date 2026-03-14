package me.moirai.storyengine.core.port.inbound.persona;

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
