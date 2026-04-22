package me.moirai.storyengine.core.port.inbound.notification;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record ReadNotification(UUID notificationId, Long requesterId) implements Command<Void> {
}
