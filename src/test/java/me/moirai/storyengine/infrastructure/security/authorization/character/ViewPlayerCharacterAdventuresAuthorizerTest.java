package me.moirai.storyengine.infrastructure.security.authorization.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterReader;

@ExtendWith(MockitoExtension.class)
public class ViewPlayerCharacterAdventuresAuthorizerTest {

    private static final UUID CHARACTER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private PlayerCharacterReader reader;

    @InjectMocks
    private ViewPlayerCharacterAdventuresAuthorizer authorizer;

    @Test
    void shouldAuthorizeWhenPrincipalIsAdminWithoutReadingTheCharacter() {

        // given
        var principal = principalWithRole(Role.ADMIN);
        var context = contextWith(principal);

        // when
        var isAuthorized = authorizer.authorize(context);

        // then
        assertThat(isAuthorized).isTrue();
        verify(reader, never()).getOwnerUsername(any(UUID.class));
    }

    @Test
    void shouldAuthorizeWhenPrincipalIsOwner() {

        // given
        var principal = principalWithRole(Role.PLAYER);
        var context = contextWith(principal);

        when(reader.getOwnerUsername(any(UUID.class))).thenReturn(Optional.of(principal.username()));

        // when
        var isAuthorized = authorizer.authorize(context);

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldNotAuthorizeWhenPrincipalIsNotOwner() {

        // given
        var principal = principalWithRole(Role.PLAYER);
        var context = contextWith(principal);

        when(reader.getOwnerUsername(any(UUID.class))).thenReturn(Optional.of("not.the.owner"));

        // when
        var isAuthorized = authorizer.authorize(context);

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeWhenCharacterIsNotFound() {

        // given
        var principal = principalWithRole(Role.PLAYER);
        var context = contextWith(principal);

        when(reader.getOwnerUsername(any(UUID.class))).thenReturn(Optional.empty());

        // when
        var isAuthorized = authorizer.authorize(context);

        // then
        assertThat(isAuthorized).isFalse();
    }

    private MoiraiPrincipal principalWithRole(Role role) {
        return new MoiraiPrincipal(
                UUID.randomUUID(),
                1L,
                "user",
                "token",
                "refresh",
                role,
                null);
    }

    private AuthorizationContext contextWith(MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("characterId", CHARACTER_ID));
    }
}
