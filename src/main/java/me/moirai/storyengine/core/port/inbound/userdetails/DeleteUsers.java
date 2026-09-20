package me.moirai.storyengine.core.port.inbound.userdetails;

import java.util.List;
import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.util.Functions;

public record DeleteUsers(
        List<UUID> userIds,
        UUID requesterId) implements Command<DeleteUsersResult> {

    public DeleteUsers {
        userIds = Functions.mapOrDefault(userIds, List.of(), List::copyOf);
    }
}
