package me.moirai.storyengine.core.port.inbound.discord.userdetails;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.enums.Role;

public record UpdateUserRole(UUID userId, Role role) implements Command<Void> {
}
