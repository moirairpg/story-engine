package me.moirai.discordbot.core.domain.notification;

import java.util.List;
import java.util.Optional;

import me.moirai.discordbot.core.application.usecase.notification.request.SearchNotifications;
import me.moirai.discordbot.core.application.usecase.notification.result.SearchNotificationsResult;

public interface NotificationRepository {

    Optional<Notification> findById(String id);

    Notification save(Notification notification);

    void deleteById(String id);

    List<Notification> findUnreadByUserId(String userId);

    List<Notification> findReadByUserId(String userId);

    SearchNotificationsResult search(SearchNotifications request);
}
