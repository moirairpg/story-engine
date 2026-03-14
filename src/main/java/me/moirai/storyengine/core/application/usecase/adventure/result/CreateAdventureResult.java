package me.moirai.storyengine.core.application.usecase.adventure.result;

public final class CreateAdventureResult {

    private final String id;

    private CreateAdventureResult(String id) {
        this.id = id;
    }

    public static CreateAdventureResult build(String id) {

        return new CreateAdventureResult(id);
    }

    public String getId() {
        return id;
    }
}
