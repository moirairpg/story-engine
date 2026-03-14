package me.moirai.storyengine.core.domain.notification;

import static me.moirai.storyengine.core.domain.notification.NotificationType.INFO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Maps.newHashMap;

import org.junit.jupiter.api.Test;

public class NotificationTest {

    @Test
    public void createNotification_whenAllDataProvided_thenInstantiate() {

        // Given
        Notification.Builder builder = Notification.builder()
                .type(INFO)
                .isGlobal(false)
                .isInteractable(false)
                .message("Some message")
                .metadata(newHashMap("something", "something"))
                .receiverDiscordId("12345")
                .senderDiscordId("12345");

        // When
        Notification result = builder.build();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isNotNull().isEqualTo("Some message");
        assertThat(result.getType()).isNotNull().isEqualTo(INFO);
        assertThat(result.getSenderDiscordId()).isNotNull().isEqualTo("12345");
        assertThat(result.getReceiverDiscordId()).isNotNull().isEqualTo("12345");
        assertThat(result.isGlobal()).isFalse();
        assertThat(result.isInteractable()).isFalse();
        assertThat(result.getMetadata()).isNotNull().isNotEmpty();
    }

    @Test
    public void createNotification_whenMetadataNull_thenInstantiateWithEmptyMetadata() {

        // Given
        Notification.Builder builder = Notification.builder()
                .type(INFO)
                .isGlobal(false)
                .isInteractable(false)
                .message("Some message")
                .metadata(null)
                .receiverDiscordId("12345")
                .senderDiscordId("12345");

        // When
        Notification result = builder.build();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isNotNull().isEqualTo("Some message");
        assertThat(result.getType()).isNotNull().isEqualTo(INFO);
        assertThat(result.getSenderDiscordId()).isNotNull().isEqualTo("12345");
        assertThat(result.getReceiverDiscordId()).isNotNull().isEqualTo("12345");
        assertThat(result.isGlobal()).isFalse();
        assertThat(result.isInteractable()).isFalse();
        assertThat(result.getMetadata()).isNotNull().isEmpty();
    }

    @Test
    public void markNotificationAsRead_whenMarkedAsRead_thenReadDataIsAvailable() {

        // Given
        String readerId = "12345";
        Notification notification = NotificationFixture.globalUnreadInfo().build();

        // When
        notification.markAsRead(readerId);

        // Then
        assertThat(notification.isReadByUserId(readerId)).isTrue();
        assertThat(notification.getReadAtByUserId(readerId)).isNotNull()
                .isNotEmpty()
                .satisfies(readDate -> assertThat(readDate.get()).isInThePast());

        assertThat(notification.getNotificationsRead()).isNotNull().isNotEmpty().hasSize(1);
    }
}
