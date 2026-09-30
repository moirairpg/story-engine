package me.moirai.storyengine.core.port.inbound.userdetails;

import java.util.List;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.util.Functions;

public record UpdateUsersActiveState(
        List<String> usernames,
        boolean isActive,
        String requesterUsername) implements Command<Void> {

    public UpdateUsersActiveState {
        usernames = Functions.mapOrDefault(usernames, List.of(), List::copyOf);
    }
}
