package me.moirai.storyengine.infrastructure.security.authorization.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.world.WorldAuthorizationReader;

@ExtendWith(MockitoExtension.class)
public class ManageWorldPermissionsAuthorizerTest {

    private static final UUID WORLD_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CALLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID STRANGER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Mock
    private WorldAuthorizationReader reader;

    @InjectMocks
    private ManageWorldPermissionsAuthorizer authorizer;

    @Test
    void shouldAuthorizeWhenCallerIsTheOwner() {

        // given
        when(reader.getAuthorizationData(any()))
                .thenReturn(Optional.of(new AssetPermissionsData(CALLER_ID, List.of(), List.of(), Visibility.PRIVATE)));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldNotAuthorizeWhenCallerIsAWriter() {

        // given
        when(reader.getAuthorizationData(any())).thenReturn(Optional.of(
                new AssetPermissionsData(STRANGER_ID, List.of(CALLER_ID), List.of(), Visibility.PRIVATE)));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeWhenCallerIsAReader() {

        // given
        when(reader.getAuthorizationData(any())).thenReturn(Optional.of(
                new AssetPermissionsData(STRANGER_ID, List.of(), List.of(CALLER_ID), Visibility.PRIVATE)));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeWhenCallerIsNotAMemberOfAPublicWorld() {

        // given
        when(reader.getAuthorizationData(any()))
                .thenReturn(Optional.of(new AssetPermissionsData(STRANGER_ID, List.of(), List.of(), Visibility.PUBLIC)));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldAuthorizeWhenCallerIsAdmin() {

        // given
        when(reader.getAuthorizationData(any()))
                .thenReturn(Optional.of(new AssetPermissionsData(STRANGER_ID, List.of(), List.of(), Visibility.PRIVATE)));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.ADMIN)));

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenWorldIsNotFound() {

        // given
        when(reader.getAuthorizationData(any())).thenReturn(Optional.empty());

        // when
        var context = contextWith(principal(Role.PLAYER));

        // then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> authorizer.authorize(context));
    }

    private MoiraiPrincipal principal(Role role) {
        return new MoiraiPrincipal(
                CALLER_ID, 1L, "caller", "token", "refresh", role, null);
    }

    private AuthorizationContext contextWith(MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("worldId", WORLD_ID));
    }
}
