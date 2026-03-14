package me.moirai.storyengine.core.port.inbound.persona;

import me.moirai.storyengine.common.usecases.UseCase;

public final class GetPersonaById extends UseCase<GetPersonaResult> {

    private final String id;
    private final String requesterId;

    private GetPersonaById(String id, String requesterId) {

        this.id = id;
        this.requesterId = requesterId;
    }

    public static GetPersonaById build(String id, String requesterId) {

        return new GetPersonaById(id, requesterId);
    }

    public String getId() {
        return id;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }
}