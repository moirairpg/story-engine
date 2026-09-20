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
import me.moirai.storyengine.infrastructure.security.authorization.user.DeleteUsersAuthorizer;

@ExtendWith(MockitoExtension.class)
class DeleteUsersAuthorizerTest {

    @InjectMocks
    private DeleteUsersAuthorizer authorizer;

    @Test
    void shouldReturnDeleteUsersOperation() {

        assertThat(authorizer.getOperation()).isEqualTo(AuthorizationOperation.DELETE_USERS);
    }

    @Test
    void shouldAuthorizeWhenRequesterIsAdmin() {

        // Given
        var context = contextWith(principalWith(Role.ADMIN));

        // When
        var result = authorizer.authorize(context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenRequesterIsPlayer() {

        // Given
        var context = contextWith(principalWith(Role.PLAYER));

        // When
        var result = authorizer.authorize(context);

        // Then
        assertThat(result).isFalse();
    }

    private MoiraiPrincipal principalWith(Role role) {
        return new MoiraiPrincipal(
                UUID.randomUUID(),
                1L,
                "12345",
                "user",
                "user@test.com",
                "token",
                "refresh",
                role,
                null);
    }

    private AuthorizationContext contextWith(MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of());
    }
}
