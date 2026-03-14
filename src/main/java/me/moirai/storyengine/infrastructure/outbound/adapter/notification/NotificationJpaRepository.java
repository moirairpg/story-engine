package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import me.moirai.storyengine.common.dbutil.PaginationRepository;
import me.moirai.storyengine.core.domain.notification.Notification;

public interface NotificationJpaRepository
        extends JpaRepository<Notification, String>, PaginationRepository<Notification, String> {

    @Query("""
                SELECT n
                  FROM Notification n
                 WHERE n.receiverDiscordId = :userId
                       AND NOT EXISTS (
                           SELECT 1 FROM NotificationRead nr
                           WHERE nr.notification = n AND nr.userId = :userId
                       )
            """)
    List<Notification> findUnreadByUserId(String userId);

    @Query("""
                SELECT n
                  FROM Notification n
                  JOIN NotificationRead nr ON nr.notification = n
                 WHERE nr.userId = :userId
            """)
    List<Notification> findReadByUserId(String userId);
}
