package me.moirai.storyengine.core.port.inbound.adventure;

import me.moirai.storyengine.common.usecases.UseCase;

public final class GetAdventureByChannelId extends UseCase<GetAdventureResult> {

    private final String channelId;
    private final String requesterId;

    private GetAdventureByChannelId(String channelId, String requesterId) {
        this.channelId = channelId;
        this.requesterId = requesterId;
    }

    public static GetAdventureByChannelId build(String channelId, String requesterId) {
        return new GetAdventureByChannelId(channelId, requesterId);
    }

    public String getChannelId() {
        return channelId;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }
}
