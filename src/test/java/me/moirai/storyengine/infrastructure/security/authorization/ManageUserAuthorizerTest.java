package me.moirai.storyengine.infrastructure.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.userdetails.UserData;
import me.moirai.storyengine.core.port.outbound.userdetails.UserReader;
import me.moirai.storyengine.infrastructure.security.authorization.user.ManageUserAuthorizer;

@ExtendWith(MockitoExtension.class)
class ManageUserAuthorizerTest {

    @Mock
    private UserReader reader;

    @InjectMocks
    private ManageUserAuthorizer authorizer;

    @Test
    void shouldReturnManageUserOperation() {

        assertThat(authorizer.getOperation()).isEqualTo(AuthorizationOperation.MANAGE_USER);
    }

    @Test
    void shouldAuthorizeWhenUserIsAdmin() {

        // Given
        var userId = UUID.randomUUID();
        var userData = adminUserData(userId);
        var principal = principalWithPublicId(UUID.randomUUID());
        var context = contextWith(userId, principal);

        when(reader.getUserById(userId)).thenReturn(Optional.of(userData));

        // When
        var result = authorizer.authorize(context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenRequesterManagesTheirOwnAccount() {

        // Given
        var userId = UUID.randomUUID();
        var userData = playerUserData(userId);
        var principal = principalWithPublicId(userId);
        var context = contextWith(userId, principal);

        when(reader.getUserById(userId)).thenReturn(Optional.of(userData));

        // When
        var result = authorizer.authorize(context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenUserIsPlayerAndRequesterIsNotSelf() {

        // Given
        var userId = UUID.randomUUID();
        var userData = playerUserData(userId);
        var principal = principalWithPublicId(UUID.randomUUID());
        var context = contextWith(userId, principal);

        when(reader.getUserById(userId)).thenReturn(Optional.of(userData));

        // When
        var result = authorizer.authorize(context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        // Given
        var userId = UUID.randomUUID();
        var principal = principalWithPublicId(UUID.randomUUID());
        var context = contextWith(userId, principal);

        when(reader.getUserById(userId)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> authorizer.authorize(context))
                .isInstanceOf(AssetNotFoundException.class);
    }

    private UserData adminUserData(UUID publicId) {
        return new UserData(publicId, 1L, "12345", Role.ADMIN, null);
    }

    private UserData playerUserData(UUID publicId) {
        return new UserData(publicId, 1L, "12345", Role.PLAYER, null);
    }

    private MoiraiPrincipal principalWithPublicId(UUID publicId) {
        return new MoiraiPrincipal(
                publicId,
                1L,
                "12345",
                "user",
                "user@test.com",
                "token",
                "refresh",
                null,
                null);
    }

    private AuthorizationContext contextWith(UUID userId, MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("userId", userId));
    }
}
