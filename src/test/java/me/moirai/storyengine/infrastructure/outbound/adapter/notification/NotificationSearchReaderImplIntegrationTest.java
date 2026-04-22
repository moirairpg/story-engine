package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.common.enums.NotificationStatus;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationRead;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotifications;
import me.moirai.storyengine.core.port.outbound.notification.NotificationSearchReader;

public class NotificationSearchReaderImplIntegrationTest extends AbstractIntegrationTest {

    private static final Long REQUESTER_ID = 1111L;

    @Autowired
    private NotificationSearchReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void search_whenAdminWithNoFilters_thenReturnAllNotifications() {

        // given
        insert(NotificationFixture.broadcast().build(), Notification.class);
        insert(NotificationFixture.system().build(), Notification.class);
        insert(NotificationFixture.game().build(), Notification.class);

        var query = new SearchNotifications(null, null, null, null, REQUESTER_ID, true, null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(3);
    }

    @Test
    public void search_whenUserWithNoFilters_thenExcludeGameAndOtherUsersNotifications() {

        // given
        insert(NotificationFixture.broadcast().build(), Notification.class);

        var system = NotificationFixture.system().build();
        ReflectionTestUtils.setField(system, "targetUserId", REQUESTER_ID);
        insert(system, Notification.class);

        var otherSystem = NotificationFixture.system().build();
        ReflectionTestUtils.setField(otherSystem, "targetUserId", 9999L);
        insert(otherSystem, Notification.class);

        insert(NotificationFixture.game().build(), Notification.class);

        var query = new SearchNotifications(null, null, null, null, REQUESTER_ID, false, null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(2);
    }

    @Test
    public void search_whenFilteredByType_thenReturnMatchingOnly() {

        // given
        insert(NotificationFixture.broadcast().build(), Notification.class);

        var system = NotificationFixture.system().build();
        ReflectionTestUtils.setField(system, "targetUserId", REQUESTER_ID);
        insert(system, Notification.class);

        var query = new SearchNotifications(NotificationType.BROADCAST, null, null, null,
                REQUESTER_ID, false, null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(1);
        assertThat(result.data().get(0).type()).isEqualTo(NotificationType.BROADCAST);
    }

    @Test
    public void search_whenFilteredByLevel_thenReturnMatchingOnly() {

        // given
        insert(NotificationFixture.broadcast().build(), Notification.class);
        insert(NotificationFixture.urgentBroadcast().build(), Notification.class);

        var query = new SearchNotifications(null, NotificationLevel.URGENT, null, null,
                REQUESTER_ID, true, null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(1);
        assertThat(result.data().get(0).level()).isEqualTo(NotificationLevel.URGENT);
    }

    @Test
    public void search_whenFilteredByStatusRead_thenReturnOnlyReadNotifications() {

        // given
        var broadcast = NotificationFixture.broadcast().build();
        var inserted = insert(broadcast, Notification.class);

        var read = NotificationRead.builder()
                .notification(inserted)
                .userId(REQUESTER_ID)
                .readDate(java.time.Instant.now())
                .build();
        insert(read, NotificationRead.class);

        insert(NotificationFixture.urgentBroadcast().build(), Notification.class);

        var query = new SearchNotifications(null, null, NotificationStatus.READ, null,
                REQUESTER_ID, true, null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(1);
        assertThat(result.data().get(0).status()).isEqualTo(NotificationStatus.READ);
    }

    @Test
    public void search_whenFilteredByStatusUnread_thenReturnOnlyUnreadNotifications() {

        // given
        insert(NotificationFixture.broadcast().build(), Notification.class);

        var query = new SearchNotifications(null, null, NotificationStatus.UNREAD, null,
                REQUESTER_ID, true, null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(1);
        assertThat(result.data().get(0).status()).isEqualTo(NotificationStatus.UNREAD);
    }

    @Test
    public void search_whenNoMatchingResults_thenReturnEmptyPage() {

        // given
        var query = new SearchNotifications(null, null, null, null, REQUESTER_ID, true, null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(0);
        assertThat(result.data()).isEmpty();
    }
}
