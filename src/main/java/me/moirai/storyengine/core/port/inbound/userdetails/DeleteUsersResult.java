package me.moirai.storyengine.core.port.inbound.userdetails;

import java.util.List;

import me.moirai.storyengine.common.util.Functions;

public record DeleteUsersResult(List<String> failedUsernames) {

    public DeleteUsersResult {
        failedUsernames = Functions.mapOrDefault(failedUsernames, List.of(), List::copyOf);
    }
}
