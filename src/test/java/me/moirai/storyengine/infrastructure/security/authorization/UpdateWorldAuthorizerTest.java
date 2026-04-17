package me.moirai.storyengine.infrastructure.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.world.WorldAuthorizationReader;
import me.moirai.storyengine.infrastructure.security.authorization.world.UpdateWorldAuthorizer;

@ExtendWith(MockitoExtension.class)
class UpdateWorldAuthorizerTest {

    private static final UUID OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID WRITER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Mock
    private WorldAuthorizationReader reader;

    @InjectMocks
    private UpdateWorldAuthorizer authorizer;

    @Test
    void shouldReturnUpdateWorldOperation() {

        assertThat(authorizer.getOperation()).isEqualTo(AuthorizationOperation.UPDATE_WORLD);
    }

    @Test
    void shouldAuthorizeWhenRequesterIsOwner() {

        // given
        var worldId = WorldFixture.PUBLIC_ID;
        var authData = worldWithPermissions();
        var principal = principalWithPublicId(OWNER_UUID);
        var context = contextWith(worldId, principal);

        when(reader.getAuthorizationData(worldId)).thenReturn(Optional.of(authData));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenRequesterIsAllowedToWrite() {

        // given
        var worldId = WorldFixture.PUBLIC_ID;
        var authData = worldWithPermissions();
        var principal = principalWithPublicId(WRITER_UUID);
        var context = contextWith(worldId, principal);

        when(reader.getAuthorizationData(worldId)).thenReturn(Optional.of(authData));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenRequesterHasNoWriteAccess() {

        // given
        var worldId = WorldFixture.PUBLIC_ID;
        var authData = worldNoPermissions();
        var principal = principalWithPublicId(UUID.randomUUID());
        var context = contextWith(worldId, principal);

        when(reader.getAuthorizationData(worldId)).thenReturn(Optional.of(authData));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenWorldNotFound() {

        // given
        var worldId = UUID.randomUUID();
        var principal = principalWithPublicId(UUID.randomUUID());
        var context = contextWith(worldId, principal);

        when(reader.getAuthorizationData(worldId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> authorizer.authorize(context))
                .isInstanceOf(NotFoundException.class);
    }

    private AssetPermissionsData worldWithPermissions() {
        return new AssetPermissionsData(OWNER_UUID, List.of(WRITER_UUID), List.of(), Visibility.PUBLIC);
    }

    private AssetPermissionsData worldNoPermissions() {
        return new AssetPermissionsData(OWNER_UUID, List.of(), List.of(), Visibility.PRIVATE);
    }

    private MoiraiPrincipal principalWithPublicId(UUID publicId) {
        return new MoiraiPrincipal(
                publicId,
                1L,
                "discordId",
                "user",
                "user@test.com",
                "token",
                "refresh",
                null,
                null);
    }

    private AuthorizationContext contextWith(UUID worldId, MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("worldId", worldId));
    }
}
