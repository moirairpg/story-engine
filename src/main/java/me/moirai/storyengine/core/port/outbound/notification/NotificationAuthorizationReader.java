package me.moirai.storyengine.core.port.outbound.notification;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.port.inbound.notification.NotificationAuthorizationData;

public interface NotificationAuthorizationReader {

    Optional<NotificationAuthorizationData> getAuthorizationData(UUID publicId);
}
