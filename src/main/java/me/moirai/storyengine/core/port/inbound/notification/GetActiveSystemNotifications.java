package me.moirai.storyengine.core.port.inbound.notification;

import java.util.List;

import me.moirai.storyengine.common.cqs.query.Query;

public record GetActiveSystemNotifications(Long userId) implements Query<List<NotificationDetails>> {
}
