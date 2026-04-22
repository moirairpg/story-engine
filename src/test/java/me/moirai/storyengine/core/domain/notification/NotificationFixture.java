package me.moirai.storyengine.core.domain.notification;

import java.util.UUID;

import org.springframework.test.util.ReflectionTestUtils;

public class NotificationFixture {

    public static final UUID PUBLIC_ID = UUID.fromString("857345aa-6666-0000-0000-000000000000");
    public static final Long NUMERIC_ID = 100L;
    public static final Long TARGET_USER_ID = 1111L;
    public static final Long ADVENTURE_ID = 2222L;

    public static Notification.Builder broadcast() {

        return Notification.builder()
                .message("Broadcast message")
                .type(NotificationType.BROADCAST)
                .level(NotificationLevel.INFO);
    }

    public static Notification.Builder urgentBroadcast() {

        return Notification.builder()
                .message("Urgent broadcast message")
                .type(NotificationType.BROADCAST)
                .level(NotificationLevel.URGENT);
    }

    public static Notification.Builder system() {

        return Notification.builder()
                .message("System message")
                .type(NotificationType.SYSTEM)
                .level(NotificationLevel.INFO)
                .targetUserId(TARGET_USER_ID);
    }

    public static Notification.Builder game() {

        return Notification.builder()
                .message("Game message")
                .type(NotificationType.GAME)
                .adventureId(ADVENTURE_ID);
    }

    public static Notification broadcastWithId() {

        var notification = broadcast().build();
        ReflectionTestUtils.setField(notification, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(notification, "publicId", PUBLIC_ID);

        return notification;
    }

    public static Notification systemWithId() {

        var notification = system().build();
        ReflectionTestUtils.setField(notification, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(notification, "publicId", PUBLIC_ID);

        return notification;
    }

    public static Notification gameWithId() {

        var notification = game().build();
        ReflectionTestUtils.setField(notification, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(notification, "publicId", PUBLIC_ID);

        return notification;
    }

    public static Notification urgentBroadcastWithId() {

        var notification = urgentBroadcast().build();
        ReflectionTestUtils.setField(notification, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(notification, "publicId", PUBLIC_ID);

        return notification;
    }
}
