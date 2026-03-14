package me.moirai.storyengine.core.application.usecase.notification.result;

import me.moirai.storyengine.core.port.inbound.notification.NotificationResult;

import static org.assertj.core.util.Maps.newHashMap;

public class NotificationResultFixture {

    public static NotificationResult.Builder targetedUnreadInfo() {

        return NotificationResult.builder()
                .message("Some message")
                .receiverDiscordId("12345")
                .senderDiscordId("12345")
                .isInteractable(false)
                .isGlobal(false)
                .type("INFO")
                .metadata(newHashMap("SomeValue", "SomeValue"));
    }

    public static NotificationResult.Builder targetedUnreadWarning() {

        return targetedUnreadInfo()
                .type("WARNING");
    }

    public static NotificationResult.Builder targetedUnreadUrgent() {

        return targetedUnreadInfo()
                .type("URGENT");
    }

    public static NotificationResult.Builder globalUnreadInfo() {

        return targetedUnreadInfo()
                .receiverDiscordId(null)
                .isGlobal(true);
    }

    public static NotificationResult.Builder globalUnreadWarning() {

        return targetedUnreadWarning()
                .receiverDiscordId(null)
                .isGlobal(true);
    }

    public static NotificationResult.Builder globalUnreadUrgent() {

        return targetedUnreadUrgent()
                .receiverDiscordId(null)
                .isGlobal(true);
    }
}
