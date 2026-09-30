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
import me.moirai.storyengine.infrastructure.security.authorization.user.ManageUserAuthorizer;

@ExtendWith(MockitoExtension.class)
class ManageUserAuthorizerTest {

    @InjectMocks
    private ManageUserAuthorizer authorizer;

    @Test
    void shouldReturnManageUserOperation() {

        assertThat(authorizer.getOperation()).isEqualTo(AuthorizationOperation.MANAGE_USER);
    }

    @Test
    void shouldAuthorizeWhenRequesterIsAdminActingOnAnotherAccount() {

        // Given
        var context = contextWith("someone.else", principalWith("merlin", Role.ADMIN));

        // When
        var result = authorizer.authorize(context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenRequesterManagesTheirOwnAccount() {

        // Given
        var context = contextWith("Merlin", principalWith("Merlin", Role.PLAYER));

        // When
        var result = authorizer.authorize(context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenRequesterManagesTheirOwnAccountInAnotherCase() {

        // Given
        var context = contextWith("mERLIN", principalWith("Merlin", Role.PLAYER));

        // When
        var result = authorizer.authorize(context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenRequesterIsPlayerActingOnAnotherAccount() {

        // Given
        var context = contextWith("someone.else", principalWith("merlin", Role.PLAYER));

        // When
        var result = authorizer.authorize(context);

        // Then
        assertThat(result).isFalse();
    }

    private MoiraiPrincipal principalWith(String username, Role role) {
        return new MoiraiPrincipal(
                UUID.randomUUID(),
                1L,
                username,
                "token",
                "refresh",
                role,
                null);
    }

    private AuthorizationContext contextWith(String username, MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("username", username));
    }
}
