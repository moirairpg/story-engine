package me.moirai.storyengine.infrastructure.inbound.rest.response;

import static org.assertj.core.util.Maps.newHashMap;

public class NotificationResponseFixture {

    public static NotificationResponse.Builder targetedUnreadInfo() {

        return NotificationResponse.builder()
                .message("Some message")
                .receiverDiscordId("12345")
                .senderDiscordId("12345")
                .isInteractable(false)
                .isGlobal(false)
                .type("INFO")
                .metadata(newHashMap("SomeValue", "SomeValue"));
    }

    public static NotificationResponse.Builder targetedUnreadWarning() {

        return targetedUnreadInfo()
                .type("WARNING");
    }

    public static NotificationResponse.Builder targetedUnreadUrgent() {

        return targetedUnreadInfo()
                .type("URGENT");
    }

    public static NotificationResponse.Builder globalUnreadInfo() {

        return targetedUnreadInfo()
                .receiverDiscordId(null)
                .isGlobal(true);
    }

    public static NotificationResponse.Builder globalUnreadWarning() {

        return targetedUnreadWarning()
                .receiverDiscordId(null)
                .isGlobal(true);
    }

    public static NotificationResponse.Builder globalUnreadUrgent() {

        return targetedUnreadUrgent()
                .receiverDiscordId(null)
                .isGlobal(true);
    }
}
