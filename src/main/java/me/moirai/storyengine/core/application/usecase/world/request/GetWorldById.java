package me.moirai.storyengine.core.application.usecase.world.request;

import me.moirai.storyengine.common.usecases.UseCase;
import me.moirai.storyengine.core.application.usecase.world.result.GetWorldResult;

public final class GetWorldById extends UseCase<GetWorldResult> {

    private final String id;
    private final String requesterId;

    private GetWorldById(String id, String requesterId) {

        this.id = id;
        this.requesterId = requesterId;
    }

    public static GetWorldById build(String id, String requesterId) {

        return new GetWorldById(id, requesterId);
    }

    public String getId() {
        return id;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }
}
