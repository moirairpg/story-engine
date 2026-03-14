package me.moirai.storyengine.core.application.usecase.persona.request;

import me.moirai.storyengine.common.usecases.UseCase;

public final class DeletePersona extends UseCase<Void> {

    private final String id;
    private final String requesterId;

    private DeletePersona(String id, String requesterId) {

        this.id = id;
        this.requesterId = requesterId;
    }

    public static DeletePersona build(String id, String requesterId) {

        return new DeletePersona(id, requesterId);
    }

    public String getId() {
        return id;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }
}
