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
import me.moirai.storyengine.core.port.inbound.notification.GetNotificationById;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;

@ExtendWith(MockitoExtension.class)
public class GetNotificationByIdHandlerTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private GetNotificationByIdHandler handler;

    @Test
    public void getNotifications_whenUserIdIsNull_thenThrowException() {

        // Given
        String notificationId = null;
        GetNotificationById request = GetNotificationById.create(notificationId);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(request));
    }

    @Test
    public void getNotifications_whenNotificationNotFound_thenThrowException() {

        // Given
        String notificationId = "12345";
        GetNotificationById request = GetNotificationById.create(notificationId);

        when(repository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(AssetNotFoundException.class)
                .isThrownBy(() -> handler.handle(request));
    }

    @Test
    public void getNotifications_whenOnlyReadFound_thenReturnNotifications() {

        // Given
        String notificationId = "12345";
        Notification notification = NotificationFixture.globalUnreadInfo().build();
        GetNotificationById request = GetNotificationById.create(notificationId);

        notification.markAsRead("12345");
        notification.markAsRead("4535");
        notification.markAsRead("86789");

        when(repository.findById(anyString())).thenReturn(Optional.of(notification));

        // When
        NotificationDetails result = handler.handle(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNotificationsRead().size()).isEqualTo(3);
    }
}
