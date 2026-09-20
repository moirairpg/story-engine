package me.moirai.storyengine.core.port.inbound.userdetails;

import java.util.List;
import java.util.UUID;

import me.moirai.storyengine.common.util.Functions;

public record DeleteUsersResult(List<UUID> failedUserIds) {

    public DeleteUsersResult {
        failedUserIds = Functions.mapOrDefault(failedUserIds, List.of(), List::copyOf);
    }
}
