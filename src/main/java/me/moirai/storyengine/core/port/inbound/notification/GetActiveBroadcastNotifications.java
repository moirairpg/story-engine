package me.moirai.storyengine.core.port.inbound.notification;

import java.util.List;

import me.moirai.storyengine.common.cqs.query.Query;

public record GetActiveBroadcastNotifications(Long requesterId) implements Query<List<NotificationDetails>> {
}
