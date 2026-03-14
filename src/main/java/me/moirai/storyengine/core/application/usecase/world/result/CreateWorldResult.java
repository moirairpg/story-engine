package me.moirai.storyengine.core.application.usecase.world.result;

public final class CreateWorldResult {

    private final String id;

    public CreateWorldResult(String id) {
        this.id = id;
    }

    public static CreateWorldResult build(String id) {

        return new CreateWorldResult(id);
    }

    public String getId() {
        return id;
    }
}
