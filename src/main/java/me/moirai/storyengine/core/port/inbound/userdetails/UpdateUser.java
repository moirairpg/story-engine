package me.moirai.storyengine.core.port.inbound.userdetails;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.enums.Role;

public record UpdateUser(
        String username,
        Role role,
        boolean isActive,
        String bio,
        String displayName,
        String requesterUsername) implements Command<Void> {
}
