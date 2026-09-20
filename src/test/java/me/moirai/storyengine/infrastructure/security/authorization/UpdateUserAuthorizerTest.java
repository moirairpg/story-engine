package me.moirai.storyengine.infrastructure.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.infrastructure.security.authorization.user.UpdateUserAuthorizer;

@ExtendWith(MockitoExtension.class)
class UpdateUserAuthorizerTest {

    @InjectMocks
    private UpdateUserAuthorizer authorizer;

    @Test
    void shouldReturnUpdateUserOperation() {

        assertThat(authorizer.getOperation()).isEqualTo(AuthorizationOperation.UPDATE_USER);
    }

    @Test
    void shouldAuthorizeWhenRequesterIsAdminActingOnAnotherAccount() {

        // Given
        var context = contextWith(UUID.randomUUID(), principalWith(UUID.randomUUID(), Role.ADMIN));

        // When
        var result = authorizer.authorize(context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenRequesterUpdatesTheirOwnAccount() {

        // Given
        var userId = UUID.randomUUID();
        var context = contextWith(userId, principalWith(userId, Role.PLAYER));

        // When
        var result = authorizer.authorize(context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenRequesterIsPlayerActingOnAnotherAccount() {

        // Given
        var context = contextWith(UUID.randomUUID(), principalWith(UUID.randomUUID(), Role.PLAYER));

        // When
        var result = authorizer.authorize(context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldDenyWhenRequesterIsPlayerActingOnAnAdminAccount() {

        // Given
        var adminId = UUID.randomUUID();
        var context = contextWith(adminId, principalWith(UUID.randomUUID(), Role.PLAYER));

        // When
        var result = authorizer.authorize(context);

        // Then
        assertThat(result).isFalse();
    }

    private MoiraiPrincipal principalWith(UUID publicId, Role role) {
        return new MoiraiPrincipal(
                publicId,
                1L,
                "12345",
                "user",
                "user@test.com",
                "token",
                "refresh",
                role,
                null);
    }

    private AuthorizationContext contextWith(UUID userId, MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("userId", userId));
    }
}
