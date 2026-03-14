package me.moirai.storyengine.core.port.inbound.world;

import me.moirai.storyengine.common.usecases.UseCase;

public final class DeleteWorld extends UseCase<Void> {

    private final String id;
    private final String requesterId;

    private DeleteWorld(String id, String requesterId) {

        this.id = id;
        this.requesterId = requesterId;
    }

    public static DeleteWorld build(String id, String requesterId) {

        return new DeleteWorld(id, requesterId);
    }

    public String getId() {
        return id;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }
}
