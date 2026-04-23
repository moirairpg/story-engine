package me.moirai.storyengine.core.port.inbound.notification;

import java.util.Collections;
import java.util.List;

import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.domain.notification.NotificationType;

public record NotificationBasicData(
        List<String> targetUsernames,
        NotificationType type) {

    public NotificationBasicData {
        targetUsernames = Functions.mapOrDefault(targetUsernames, List.of(), Collections::unmodifiableList);
    }
}
