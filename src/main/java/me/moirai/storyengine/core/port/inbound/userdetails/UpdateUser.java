package me.moirai.storyengine.core.port.inbound.userdetails;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.enums.Role;

public record UpdateUser(
        UUID userId,
        Role role,
        boolean isActive,
        String bio,
        UUID requesterId) implements Command<Void> {
}
