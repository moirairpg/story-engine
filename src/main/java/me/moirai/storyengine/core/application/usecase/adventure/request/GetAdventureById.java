package me.moirai.storyengine.core.application.usecase.adventure.request;

import me.moirai.storyengine.common.usecases.UseCase;
import me.moirai.storyengine.core.application.usecase.adventure.result.GetAdventureResult;

public final class GetAdventureById extends UseCase<GetAdventureResult> {

    private final String id;
    private final String requesterId;

    private GetAdventureById(String id, String requesterId) {
        this.id = id;
        this.requesterId = requesterId;
    }

    public static GetAdventureById build(String id, String requesterId) {
        return new GetAdventureById(id, requesterId);
    }

    public String getId() {
        return id;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }
}
