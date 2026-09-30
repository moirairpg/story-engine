package me.moirai.storyengine.infrastructure.security.authorization.adventure;

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

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureReader;

@ExtendWith(MockitoExtension.class)
public class PlayAdventureAuthorizerTest {

    private static final UUID ADVENTURE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CALLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID STRANGER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Mock
    private AdventureReader reader;

    @InjectMocks
    private PlayAdventureAuthorizer authorizer;

    @Test
    void shouldExposeThePlayAdventureOperation() {

        // when
        var operation = authorizer.getOperation();

        // then
        assertThat(operation).isEqualTo(AuthorizationOperation.PLAY_ADVENTURE);
    }

    @Test
    void shouldAuthorizeAnEnrolledPlayer() {

        // given
        when(reader.getEnrolledPlayerIds(any()))
                .thenReturn(List.of(STRANGER_ID, CALLER_ID));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldNotAuthorizeAPlayerWithoutAnEnrolledCharacter() {

        // given
        when(reader.getEnrolledPlayerIds(any()))
                .thenReturn(List.of(STRANGER_ID));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeWhenTheRosterIsEmpty() {

        // given
        when(reader.getEnrolledPlayerIds(any()))
                .thenReturn(List.of());

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeAnAdminWithoutAnEnrolledCharacter() {

        // given
        when(reader.getEnrolledPlayerIds(any()))
                .thenReturn(List.of(STRANGER_ID));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.ADMIN)));

        // then
        assertThat(isAuthorized).isFalse();
    }

    private MoiraiPrincipal principal(Role role) {
        return new MoiraiPrincipal(
                CALLER_ID, 1L, "caller", "token", "refresh", role, null);
    }

    private AuthorizationContext contextWith(MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("adventureId", ADVENTURE_ID));
    }
}
