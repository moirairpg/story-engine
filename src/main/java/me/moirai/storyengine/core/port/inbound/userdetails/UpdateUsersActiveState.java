package me.moirai.storyengine.core.port.inbound.userdetails;

import java.util.List;
import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.util.Functions;

public record UpdateUsersActiveState(
        List<UUID> userIds,
        boolean isActive,
        UUID requesterId) implements Command<Void> {

    public UpdateUsersActiveState {
        userIds = Functions.mapOrDefault(userIds, List.of(), List::copyOf);
    }
}
