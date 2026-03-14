package me.moirai.storyengine.core.port.inbound.adventure;

public final class CreateAdventureLorebookEntryResult {

    private final String id;

    public CreateAdventureLorebookEntryResult(String id) {
        this.id = id;
    }

    public static CreateAdventureLorebookEntryResult build(String id) {

        return new CreateAdventureLorebookEntryResult(id);
    }

    public String getId() {
        return id;
    }
}
