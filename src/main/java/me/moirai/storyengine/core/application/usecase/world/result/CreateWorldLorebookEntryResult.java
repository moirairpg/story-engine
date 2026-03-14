package me.moirai.storyengine.core.application.usecase.world.result;

public final class CreateWorldLorebookEntryResult {

    private final String id;

    public CreateWorldLorebookEntryResult(String id) {
        this.id = id;
    }

    public static CreateWorldLorebookEntryResult build(String id) {

        return new CreateWorldLorebookEntryResult(id);
    }

    public String getId() {
        return id;
    }
}
