package me.moirai.storyengine.core.application.usecase.adventure.request;

import me.moirai.storyengine.common.usecases.UseCase;

public final class DeleteAdventure extends UseCase<Void> {

    private final String id;
    private final String requesterId;

    private DeleteAdventure(String id, String requesterId) {
        this.id = id;
        this.requesterId = requesterId;
    }

    public static DeleteAdventure build(String id, String requesterId) {

        return new DeleteAdventure(id, requesterId);
    }

    public String getId() {
        return id;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }
}
