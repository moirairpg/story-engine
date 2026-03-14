package me.moirai.storyengine.core.application.usecase.notification;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.notification.request.GetNotificationsByUserId;
import me.moirai.storyengine.core.application.usecase.notification.result.NotificationResult;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.domain.notification.NotificationRepository;

@ExtendWith(MockitoExtension.class)
public class GetNotificationsByUserIdHandlerTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private GetNotificationsByUserIdHandler handler;

    @Test
    public void getNotifications_whenUserIdIsNull_thenThrowException() {

        // Given
        String userId = null;
        GetNotificationsByUserId request = GetNotificationsByUserId.create(userId);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(request));
    }

    @Test
    public void getNotifications_whenOnlyReadFound_thenReturnNotifications() {

        // Given
        String userId = "12345";
        Notification readNotification = NotificationFixture.globalUnreadInfo().build();
        List<Notification> notifications = list(readNotification);
        GetNotificationsByUserId request = GetNotificationsByUserId.create(userId);

        readNotification.markAsRead(userId);

        when(repository.findReadByUserId(anyString())).thenReturn(notifications);
        when(repository.findUnreadByUserId(anyString())).thenReturn(emptyList());

        // When
        List<NotificationResult> result = handler.handle(request);

        // Then
        assertThat(result).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.get(0).getMessage()).isEqualTo(notifications.get(0).getMessage());
        assertThat(result.get(0).getNotificationsRead()).hasSize(1);
        assertThat(result.get(0).getNotificationsRead().get(0).getUserId()).isEqualTo(userId);
        assertThat(result.get(0).getNotificationsRead().get(0).getReadAt()).isInThePast();
    }

    @Test
    public void getNotifications_whenOnlyUnreadFound_thenReturnNotifications() {

        // Given
        String userId = "12345";
        List<Notification> notifications = list(NotificationFixture.globalUnreadInfo().build());
        GetNotificationsByUserId request = GetNotificationsByUserId.create(userId);

        when(repository.findReadByUserId(anyString())).thenReturn(emptyList());
        when(repository.findUnreadByUserId(anyString())).thenReturn(notifications);

        // When
        List<NotificationResult> result = handler.handle(request);

        // Then
        assertThat(result).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.get(0).getMessage()).isEqualTo(notifications.get(0).getMessage());
        assertThat(result.get(0).getNotificationsRead().size()).isZero();
    }

    @Test
    public void getNotifications_whenFound_thenReturnNotifications() {

        // Given
        String userId = "12345";
        Notification readNotification = NotificationFixture.globalUnreadInfo().build();
        Notification unreadNotification = NotificationFixture.globalUnreadInfo().build();
        List<Notification> notifications = list(readNotification, unreadNotification);
        GetNotificationsByUserId request = GetNotificationsByUserId.create(userId);

        readNotification.markAsRead(userId);

        when(repository.findReadByUserId(anyString())).thenReturn(notifications);
        when(repository.findUnreadByUserId(anyString())).thenReturn(emptyList());

        // When
        List<NotificationResult> result = handler.handle(request);

        // Then
        assertThat(result).isNotNull().isNotEmpty().hasSize(2);
        assertThat(result.get(0).getMessage()).isEqualTo(notifications.get(0).getMessage());
        assertThat(result.get(1).getMessage()).isEqualTo(notifications.get(1).getMessage());
        assertThat(result.get(0).getNotificationsRead().size()).isOne();
    }
}
