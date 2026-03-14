package me.moirai.storyengine.core.application.usecase.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.core.application.usecase.notification.request.ReadNotification;
import me.moirai.storyengine.core.application.usecase.notification.result.NotificationReadResult;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.domain.notification.NotificationRepository;

@ExtendWith(MockitoExtension.class)
public class ReadNotificationHandlerTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private ReadNotificationHandler handler;

    @Test
    public void readNotification_whenNotificationIdEmpty_thenThrowException() {

        // Given
        String userId = "123123";
        String notificationId = null;

        ReadNotification request = ReadNotification.create(userId, notificationId);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(request));
    }

    @Test
    public void readNotification_whenUserIdEmpty_thenThrowException() {

        // Given
        String userId = null;
        String notificationId = "123123";

        ReadNotification request = ReadNotification.create(userId, notificationId);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(request));
    }

    @Test
    public void readNotification_whenNotificationNotFound_thenThrowException() {

        // Given
        String userId = "123123";
        String notificationId = "123123";

        ReadNotification request = ReadNotification.create(userId, notificationId);

        when(repository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(AssetNotFoundException.class)
                .isThrownBy(() -> handler.handle(request));
    }

    @Test
    public void readNotification_whenNotificationAlreadyRead_thenThrowException() {

        // Given
        String userId = "123123";
        String notificationId = "123123";

        ReadNotification request = ReadNotification.create(userId, notificationId);

        Notification notification = NotificationFixture.globalUnreadInfo().build();
        notification.markAsRead(userId);

        when(repository.findById(anyString())).thenReturn(Optional.of(notification));

        // Then
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> handler.handle(request));
    }

    @Test
    public void readNotification_whenValidRequest_thenNotificationIsRead() {

        // Given
        String userId = "123123";
        String notificationId = "123123";

        ReadNotification request = ReadNotification.create(userId, notificationId);

        Notification notification = NotificationFixture.globalUnreadInfo().build();

        when(repository.findById(anyString())).thenReturn(Optional.of(notification));

        // When
        NotificationReadResult result = handler.handle(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReadAt()).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
    }
}
