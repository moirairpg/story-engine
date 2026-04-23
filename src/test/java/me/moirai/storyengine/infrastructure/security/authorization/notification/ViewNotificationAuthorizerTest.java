package me.moirai.storyengine.infrastructure.security.authorization.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.port.inbound.notification.GetNotificationBasicData;
import me.moirai.storyengine.core.port.inbound.notification.NotificationBasicData;

@ExtendWith(MockitoExtension.class)
class ViewNotificationAuthorizerTest {

    @Mock
    private QueryRunner queryRunner;

    @InjectMocks
    private ViewNotificationAuthorizer authorizer;

    @Test
    void shouldReturnTrueWhenPrincipalIsAdmin() {

        // given
        var notificationId = UUID.randomUUID();
        var basicData = new NotificationBasicData(List.of("alice"), NotificationType.SYSTEM);
        var principal = principalWith("admin", Role.ADMIN);
        var context = contextWith(notificationId, principal);

        when(queryRunner.run(any(GetNotificationBasicData.class))).thenReturn(basicData);

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueWhenPrincipalUsernameIsInRecipients() {

        // given
        var notificationId = UUID.randomUUID();
        var basicData = new NotificationBasicData(List.of("alice", "bob"), NotificationType.SYSTEM);
        var principal = principalWith("alice", Role.PLAYER);
        var context = contextWith(notificationId, principal);

        when(queryRunner.run(any(GetNotificationBasicData.class))).thenReturn(basicData);

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenPrincipalUsernameIsNotInRecipients() {

        // given
        var notificationId = UUID.randomUUID();
        var basicData = new NotificationBasicData(List.of("alice", "bob"), NotificationType.SYSTEM);
        var principal = principalWith("charlie", Role.PLAYER);
        var context = contextWith(notificationId, principal);

        when(queryRunner.run(any(GetNotificationBasicData.class))).thenReturn(basicData);

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenBroadcastAndRecipientsEmpty() {

        // given
        var notificationId = UUID.randomUUID();
        var basicData = new NotificationBasicData(List.of(), NotificationType.BROADCAST);
        var principal = principalWith("anyone", Role.PLAYER);
        var context = contextWith(notificationId, principal);

        when(queryRunner.run(any(GetNotificationBasicData.class))).thenReturn(basicData);

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSystemButRecipientsEmpty() {

        // given
        var notificationId = UUID.randomUUID();
        var basicData = new NotificationBasicData(List.of(), NotificationType.SYSTEM);
        var principal = principalWith("alice", Role.PLAYER);
        var context = contextWith(notificationId, principal);

        when(queryRunner.run(any(GetNotificationBasicData.class))).thenReturn(basicData);

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTypeIsGameAndNotAdmin() {

        // given
        var notificationId = UUID.randomUUID();
        var basicData = new NotificationBasicData(List.of(), NotificationType.GAME);
        var principal = principalWith("alice", Role.PLAYER);
        var context = contextWith(notificationId, principal);

        when(queryRunner.run(any(GetNotificationBasicData.class))).thenReturn(basicData);

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isFalse();
    }

    private MoiraiPrincipal principalWith(String username, Role role) {
        return new MoiraiPrincipal(
                UUID.randomUUID(),
                1L,
                "discordId",
                username,
                username + "@test.com",
                "token",
                "refresh",
                role,
                null);
    }

    private AuthorizationContext contextWith(UUID notificationId, MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("notificationId", notificationId));
    }
}
