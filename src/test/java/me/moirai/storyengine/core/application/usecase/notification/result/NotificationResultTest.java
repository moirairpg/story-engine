package me.moirai.storyengine.core.application.usecase.notification.result;

import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Maps.newHashMap;

import org.junit.jupiter.api.Test;

public class NotificationResultTest {

    @Test
    public void createNotification_whenAllDataProvided_thenInstantiate() {

        // Given
        NotificationDetails.Builder builder = NotificationDetails.builder()
                .type("INFO")
                .isGlobal(false)
                .isInteractable(false)
                .message("Some message")
                .metadata(newHashMap("something", "something"))
                .receiverDiscordId("12345")
                .senderDiscordId("12345");

        // When
        NotificationDetails result = builder.build();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isNotNull().isEqualTo("Some message");
        assertThat(result.getType()).isNotNull().isEqualTo("INFO");
        assertThat(result.getSenderDiscordId()).isNotNull().isEqualTo("12345");
        assertThat(result.getReceiverDiscordId()).isNotNull().isEqualTo("12345");
        assertThat(result.isGlobal()).isFalse();
        assertThat(result.isInteractable()).isFalse();
        assertThat(result.getMetadata()).isNotNull().isNotEmpty();
    }

    @Test
    public void createNotification_whenMetadataNull_thenInstantiateWithEmptyMetadata() {

        // Given
        NotificationDetails.Builder builder = NotificationDetails.builder()
                .type("INFO")
                .isGlobal(false)
                .isInteractable(false)
                .message("Some message")
                .metadata(null)
                .receiverDiscordId("12345")
                .senderDiscordId("12345");

        // When
        NotificationDetails result = builder.build();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isNotNull().isEqualTo("Some message");
        assertThat(result.getType()).isNotNull().isEqualTo("INFO");
        assertThat(result.getSenderDiscordId()).isNotNull().isEqualTo("12345");
        assertThat(result.getReceiverDiscordId()).isNotNull().isEqualTo("12345");
        assertThat(result.isGlobal()).isFalse();
        assertThat(result.isInteractable()).isFalse();
        assertThat(result.getMetadata()).isNotNull().isEmpty();
    }
}
