package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.set;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotifications;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotificationsResult;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.domain.notification.NotificationType;

@Sql(statements = { "DELETE FROM notification_read", "DELETE FROM notification" })
public class NotificationRepositoryImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private NotificationJpaRepository jpaRepository;

    @Autowired
    private NotificationRepository repository;

    @Test
    public void findById() {

        // Given
        Notification notification = jpaRepository.save(NotificationFixture.globalUnreadInfo().build());

        // When
        Optional<Notification> result = repository.findById(notification.getId());

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result.get().getVersion()).isOne();
        assertThat(result.get().getCreationDate()).isInThePast();
        assertThat(result.get().getLastUpdateDate()).isInThePast();
    }

    @Test
    public void saveNotification() {

        // Given
        Notification notification = repository.save(NotificationFixture.globalUnreadInfo().build());

        // Then
        assertThat(jpaRepository.existsById(notification.getId())).isTrue();
    }

    @Test
    public void deleteNotification() {

        // Given
        Notification notification = repository.save(NotificationFixture.globalUnreadInfo().build());

        // When
        repository.deleteById(notification.getId());

        // Then
        assertThat(jpaRepository.existsById(notification.getId())).isFalse();
    }

    @Test
    public void getUnreadMessages() {

        // Given
        String receiverId = "12345";
        repository.save(NotificationFixture.targetedUnreadInfo()
                .id(null)
                .receiverDiscordId(receiverId)
                .message("Unread 1")
                .build());

        repository.save(NotificationFixture.targetedUnreadInfo()
                .id(null)
                .receiverDiscordId(receiverId)
                .message("Unread 2")
                .build());

        Notification read1 = NotificationFixture.targetedUnreadInfo()
                .id(null)
                .receiverDiscordId(receiverId)
                .message("Read 1")
                .build();

        Notification read2 = NotificationFixture.targetedUnreadInfo()
                .id(null)
                .receiverDiscordId(receiverId)
                .message("Read 2")
                .build();

        read1.markAsRead(receiverId);
        read2.markAsRead(receiverId);

        repository.save(read1);
        repository.save(read2);

        // When
        List<Notification> result = repository.findUnreadByUserId(receiverId);

        // Then
        assertThat(result).isNotNull()
                .isNotEmpty()
                .hasSize(2)
                .extracting(Notification::getMessage)
                .containsExactlyInAnyOrder("Unread 1", "Unread 2");
    }

    @Test
    public void getReadMessages() {

        // Given
        String receiverId = "12345";
        repository.save(NotificationFixture.targetedUnreadInfo()
                .id(null)
                .receiverDiscordId(receiverId)
                .message("Unread 1")
                .build());

        repository.save(NotificationFixture.targetedUnreadInfo()
                .id(null)
                .receiverDiscordId(receiverId)
                .message("Unread 2")
                .build());

        Notification read1 = NotificationFixture.targetedUnreadInfo()
                .id(null)
                .receiverDiscordId(receiverId)
                .message("Read 1")
                .build();

        Notification read2 = NotificationFixture.targetedUnreadInfo()
                .id(null)
                .receiverDiscordId(receiverId)
                .message("Read 2")
                .build();

        read1.markAsRead(receiverId);
        read2.markAsRead(receiverId);

        repository.save(read1);
        repository.save(read2);

        // When
        List<Notification> result = repository.findReadByUserId(receiverId);

        // Then
        assertThat(result).isNotNull()
                .isNotEmpty()
                .hasSize(2)
                .extracting(Notification::getMessage)
                .containsExactlyInAnyOrder("Read 1", "Read 2");
    }

    @Test
    public void searchNotificationOrderByTypeAsc() {

        // Given
        Notification urgent = NotificationFixture.globalUnreadInfo()
                .id(null)
                .type(NotificationType.URGENT)
                .build();

        Notification info = NotificationFixture.globalUnreadInfo()
                .id(null)
                .type(NotificationType.INFO)
                .build();

        Notification warning = NotificationFixture.globalUnreadInfo()
                .id(null)
                .type(NotificationType.WARNING)
                .build();

        jpaRepository.saveAll(set(urgent, info, warning));

        SearchNotifications query = SearchNotifications.builder()
                .sortingField("type")
                .page(1)
                .size(10)
                .build();

        // When
        SearchNotificationsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<NotificationDetails> notifications = result.getResults();
        assertThat(notifications.get(0).getType()).isEqualTo(info.getType().name());
        assertThat(notifications.get(1).getType()).isEqualTo(urgent.getType().name());
        assertThat(notifications.get(2).getType()).isEqualTo(warning.getType().name());
    }

    @Test
    public void searchNotificationOrderByTypeDesc() {

        // Given
        Notification urgent = NotificationFixture.globalUnreadInfo()
                .id(null)
                .type(NotificationType.URGENT)
                .build();

        Notification info = NotificationFixture.globalUnreadInfo()
                .id(null)
                .type(NotificationType.INFO)
                .build();

        Notification warning = NotificationFixture.globalUnreadInfo()
                .id(null)
                .type(NotificationType.WARNING)
                .build();

        jpaRepository.saveAll(set(urgent, info, warning));

        SearchNotifications query = SearchNotifications.builder()
                .sortingField("type")
                .direction("DESC")
                .build();

        // When
        SearchNotificationsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<NotificationDetails> notifications = result.getResults();
        assertThat(notifications.get(0).getType()).isEqualTo(warning.getType().name());
        assertThat(notifications.get(1).getType()).isEqualTo(urgent.getType().name());
        assertThat(notifications.get(2).getType()).isEqualTo(info.getType().name());
    }

    @Test
    public void searchNotificationFilterBySenderId() {

        // Given
        Notification urgent = NotificationFixture.globalUnreadInfo()
                .id(null)
                .senderDiscordId("12345")
                .build();

        Notification info = NotificationFixture.globalUnreadInfo()
                .id(null)
                .senderDiscordId("123123")
                .build();

        Notification warning = NotificationFixture.globalUnreadInfo()
                .id(null)
                .senderDiscordId("5678568")
                .build();

        jpaRepository.saveAll(set(urgent, info, warning));

        SearchNotifications query = SearchNotifications.builder()
                .senderDiscordId("123")
                .build();

        // When
        SearchNotificationsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<NotificationDetails> notifications = result.getResults();
        assertThat(notifications.get(0).getSenderDiscordId()).isEqualTo(urgent.getSenderDiscordId());
        assertThat(notifications.get(1).getSenderDiscordId()).isEqualTo(info.getSenderDiscordId());
    }

    @Test
    public void searchNotificationFilterByReceiverId() {

        // Given
        Notification urgent = NotificationFixture.globalUnreadInfo()
                .id(null)
                .receiverDiscordId("12345")
                .build();

        Notification info = NotificationFixture.globalUnreadInfo()
                .id(null)
                .receiverDiscordId("123123")
                .build();

        Notification warning = NotificationFixture.globalUnreadInfo()
                .id(null)
                .receiverDiscordId("5678568")
                .build();

        jpaRepository.saveAll(set(urgent, info, warning));

        SearchNotifications query = SearchNotifications.builder()
                .receiverDiscordId("123")
                .build();

        // When
        SearchNotificationsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<NotificationDetails> notifications = result.getResults();
        assertThat(notifications.get(0).getReceiverDiscordId()).isEqualTo(urgent.getReceiverDiscordId());
        assertThat(notifications.get(1).getReceiverDiscordId()).isEqualTo(info.getReceiverDiscordId());
    }

    @Test
    public void searchNotificationFilterByType() {

        // Given
        Notification urgent = NotificationFixture.globalUnreadInfo()
                .id(null)
                .type(NotificationType.URGENT)
                .build();

        Notification info = NotificationFixture.globalUnreadInfo()
                .id(null)
                .type(NotificationType.INFO)
                .build();

        Notification warning = NotificationFixture.globalUnreadInfo()
                .id(null)
                .type(NotificationType.WARNING)
                .build();

        jpaRepository.saveAll(set(urgent, info, warning));

        SearchNotifications query = SearchNotifications.builder()
                .type("INFO")
                .build();

        // When
        SearchNotificationsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<NotificationDetails> notifications = result.getResults();
        assertThat(notifications.get(0).getType()).isEqualTo(info.getType().name());
    }

    @Test
    public void searchNotificationFilterByGlobal() {

        // Given
        Notification urgent = NotificationFixture.globalUnreadInfo()
                .id(null)
                .isGlobal(false)
                .build();

        Notification info = NotificationFixture.globalUnreadInfo()
                .id(null)
                .isGlobal(true)
                .build();

        Notification warning = NotificationFixture.globalUnreadInfo()
                .id(null)
                .isGlobal(false)
                .build();

        jpaRepository.saveAll(set(urgent, info, warning));

        SearchNotifications query = SearchNotifications.builder()
                .isGlobal(true)
                .build();

        // When
        SearchNotificationsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<NotificationDetails> notifications = result.getResults();
        assertThat(notifications.get(0).isGlobal()).isEqualTo(info.isGlobal());
    }

    @Test
    public void searchNotificationFilterByInteractable() {

        // Given
        Notification urgent = NotificationFixture.globalUnreadInfo()
                .id(null)
                .isInteractable(false)
                .build();

        Notification info = NotificationFixture.globalUnreadInfo()
                .id(null)
                .isInteractable(true)
                .build();

        Notification warning = NotificationFixture.globalUnreadInfo()
                .id(null)
                .isInteractable(false)
                .build();

        jpaRepository.saveAll(set(urgent, info, warning));

        SearchNotifications query = SearchNotifications.builder()
                .isInteractable(true)
                .build();

        // When
        SearchNotificationsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<NotificationDetails> notifications = result.getResults();
        assertThat(notifications.get(0).isInteractable()).isEqualTo(info.isInteractable());
    }
}
