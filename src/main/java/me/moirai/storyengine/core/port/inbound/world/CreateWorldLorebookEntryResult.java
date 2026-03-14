package me.moirai.storyengine.core.port.inbound.world;

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
