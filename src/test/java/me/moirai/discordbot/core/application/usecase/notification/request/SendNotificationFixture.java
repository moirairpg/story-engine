package me.moirai.discordbot.core.application.usecase.notification.request;

public class SendNotificationFixture {

    public static SendNotification.Builder targetedUnreadInfo() {

        return SendNotification.builder()
                .message("Some message")
                .receiverDiscordId("12345")
                .senderDiscordId("12345")
                .isInteractable(false)
                .isGlobal(false)
                .type("INFO");
    }

    public static SendNotification.Builder globalUnreadInfo() {

        return targetedUnreadInfo()
                .receiverDiscordId(null)
                .isGlobal(true);
    }
}
