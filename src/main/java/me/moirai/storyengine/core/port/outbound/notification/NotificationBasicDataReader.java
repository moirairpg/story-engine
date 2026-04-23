package me.moirai.storyengine.core.port.outbound.notification;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.port.inbound.notification.NotificationBasicData;

public interface NotificationBasicDataReader {

    Optional<NotificationBasicData> getByPublicId(UUID publicId);
}
