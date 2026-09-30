package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.List;

import me.moirai.storyengine.common.util.Functions;

public record InviteUserToAdventureResult(List<String> invited, List<String> notFound) {

    public InviteUserToAdventureResult {
        invited = Functions.mapOrDefault(invited, List.of(), List::copyOf);
        notFound = Functions.mapOrDefault(notFound, List.of(), List::copyOf);
    }
}
