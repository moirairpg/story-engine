package me.moirai.storyengine.core.application.usecase.notification.result;

import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;

import static org.assertj.core.util.Maps.newHashMap;

public class NotificationResultFixture {

    public static NotificationDetails.Builder targetedUnreadInfo() {

        return NotificationDetails.builder()
                .message("Some message")
                .receiverDiscordId("12345")
                .senderDiscordId("12345")
                .isInteractable(false)
                .isGlobal(false)
                .type("INFO")
                .metadata(newHashMap("SomeValue", "SomeValue"));
    }

    public static NotificationDetails.Builder targetedUnreadWarning() {

        return targetedUnreadInfo()
                .type("WARNING");
    }

    public static NotificationDetails.Builder targetedUnreadUrgent() {

        return targetedUnreadInfo()
                .type("URGENT");
    }

    public static NotificationDetails.Builder globalUnreadInfo() {

        return targetedUnreadInfo()
                .receiverDiscordId(null)
                .isGlobal(true);
    }

    public static NotificationDetails.Builder globalUnreadWarning() {

        return targetedUnreadWarning()
                .receiverDiscordId(null)
                .isGlobal(true);
    }

    public static NotificationDetails.Builder globalUnreadUrgent() {

        return targetedUnreadUrgent()
                .receiverDiscordId(null)
                .isGlobal(true);
    }
}
