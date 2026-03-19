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

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import me.moirai.storyengine.infrastructure.security.authorization.user.ManageUserAuthorizer;

@ExtendWith(MockitoExtension.class)
class ManageUserAuthorizerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ManageUserAuthorizer authorizer;

    @Test
    void shouldReturnManageUserOperation() {

        assertThat(authorizer.getOperation()).isEqualTo(AuthorizationOperation.MANAGE_USER);
    }

    @Test
    void shouldAuthorizeWhenUserIsAdmin() {

        var userId = "12345";
        var user = UserFixture.adminWithId();
        var principal = principalWithDiscordId("999999999999999");
        var context = contextWith(userId, principal);

        when(userRepository.findByDiscordId(userId)).thenReturn(Optional.of(user));

        var result = authorizer.authorize(context);

        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenRequesterManagesTheirOwnAccount() {

        var userId = "12345";
        var user = UserFixture.playerWithId();
        var principal = principalWithDiscordId(userId);
        var context = contextWith(userId, principal);

        when(userRepository.findByDiscordId(userId)).thenReturn(Optional.of(user));

        var result = authorizer.authorize(context);

        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenUserIsPlayerAndRequesterIsNotSelf() {

        var userId = "12345";
        var user = UserFixture.playerWithId();
        var principal = principalWithDiscordId("999999999999999");
        var context = contextWith(userId, principal);

        when(userRepository.findByDiscordId(userId)).thenReturn(Optional.of(user));

        var result = authorizer.authorize(context);

        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        var userId = "nonexistent";
        var principal = principalWithDiscordId("586678721356875");
        var context = contextWith(userId, principal);

        when(userRepository.findByDiscordId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorizer.authorize(context))
                .isInstanceOf(AssetNotFoundException.class);
    }

    private MoiraiPrincipal principalWithDiscordId(String discordId) {
        return new MoiraiPrincipal(
                UUID.randomUUID(),
                discordId,
                "user",
                "user@test.com",
                "token",
                "refresh",
                null,
                null);
    }

    private AuthorizationContext contextWith(String userId, MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("discordUserId", userId));
    }
}
