package me.moirai.storyengine.core.domain.notification;

import static me.moirai.storyengine.core.domain.notification.NotificationType.INFO;
import static me.moirai.storyengine.core.domain.notification.NotificationType.URGENT;
import static me.moirai.storyengine.core.domain.notification.NotificationType.WARNING;
import static org.assertj.core.util.Maps.newHashMap;

public class NotificationFixture {

    public static Notification.Builder targetedUnreadInfo() {

        return Notification.builder()
                .id("12345")
                .message("Some message")
                .receiverDiscordId("12345")
                .senderDiscordId("12345")
                .isInteractable(false)
                .isGlobal(false)
                .type(INFO)
                .metadata(newHashMap("SomeValue", "SomeValue"));
    }

    public static Notification.Builder targetedUnreadWarning() {

        return targetedUnreadInfo()
                .type(WARNING);
    }

    public static Notification.Builder targetedUnreadUrgent() {

        return targetedUnreadInfo()
                .type(URGENT);
    }

    public static Notification.Builder globalUnreadInfo() {

        return targetedUnreadInfo()
                .receiverDiscordId(null)
                .isGlobal(true);
    }

    public static Notification.Builder globalUnreadWarning() {

        return targetedUnreadWarning()
                .receiverDiscordId(null)
                .isGlobal(true);
    }

    public static Notification.Builder globalUnreadUrgent() {

        return targetedUnreadUrgent()
                .receiverDiscordId(null)
                .isGlobal(true);
    }
}
