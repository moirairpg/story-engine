package me.moirai.storyengine.core.application.usecase.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.notification.request.SendNotificationFixture;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.port.inbound.notification.SendNotification;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;

@ExtendWith(MockitoExtension.class)
public class SendNotificationHandlerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private SendNotificationHandler handler;

    @Test
    public void sendNotification_whenReceiverIdIsNull_andIsNotGlobal_thenThrowException() {

        // Given
        SendNotification request = SendNotificationFixture.targetedUnreadInfo()
                .receiverDiscordId(null)
                .isGlobal(false)
                .build();

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(request));
    }

    @Test
    public void sendNotification_whenSenderIdIsNull_thenThrowException() {

        // Given
        SendNotification request = SendNotificationFixture.targetedUnreadInfo()
                .senderDiscordId(null)
                .build();

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(request));
    }

    @Test
    public void sendNotification_whenMessageIsEmpty_thenThrowException() {

        // Given
        SendNotification request = SendNotificationFixture.targetedUnreadInfo()
                .message(null)
                .build();

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(request));
    }

    @Test
    public void sendNotification_whenTypeIsEmpty_thenThrowException() {

        // Given
        SendNotification request = SendNotificationFixture.targetedUnreadInfo()
                .type(null)
                .build();

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(request));
    }

    @Test
    public void sendNotification_targetedNotification_thenSendNotification() {

        // Given
        SendNotification request = SendNotificationFixture.targetedUnreadInfo().build();
        Notification expectedNotification = NotificationFixture.targetedUnreadInfo()
                .creationDate(OffsetDateTime.parse("2025-01-02T12:00:00.000000Z"))
                .build();

        when(notificationRepository.save(any())).thenReturn(expectedNotification);

        // When
        NotificationDetails result = handler.handle(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expectedNotification.getId());
        assertThat(result.getCreationDate()).isEqualTo(expectedNotification.getCreationDate());
    }

    @Test
    public void sendNotification_globalNotification_thenSendNotification() {

        // Given
        SendNotification request = SendNotificationFixture.globalUnreadInfo().build();
        Notification expectedNotification = NotificationFixture.targetedUnreadInfo()
                .creationDate(OffsetDateTime.parse("2025-01-02T12:00:00.000000Z"))
                .build();

        when(notificationRepository.save(any())).thenReturn(expectedNotification);

        // When
        NotificationDetails result = handler.handle(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expectedNotification.getId());
        assertThat(result.getCreationDate()).isEqualTo(expectedNotification.getCreationDate());
    }
}
