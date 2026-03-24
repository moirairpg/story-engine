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
import me.moirai.storyengine.core.domain.PermissionFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import me.moirai.storyengine.infrastructure.security.authorization.world.DeleteWorldAuthorizer;

@ExtendWith(MockitoExtension.class)
class DeleteWorldAuthorizerTest {

    @Mock
    private WorldRepository worldRepository;

    @InjectMocks
    private DeleteWorldAuthorizer authorizer;

    @Test
    void shouldReturnDeleteWorldOperation() {

        assertThat(authorizer.getOperation()).isEqualTo(AuthorizationOperation.DELETE_WORLD);
    }

    @Test
    void shouldAuthorizeWhenRequesterIsOwner() {

        // given
        var worldId = WorldFixture.PUBLIC_ID;
        var world = WorldFixture.publicWorldWithIdAndPermissions();
        var principal = principalWithId(PermissionFixture.OWNER_ID);
        var context = contextWith(worldId, principal);

        when(worldRepository.findByPublicId(worldId)).thenReturn(Optional.of(world));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenRequesterIsAllowedToWrite() {

        // given
        var worldId = WorldFixture.PUBLIC_ID;
        var world = WorldFixture.publicWorldWithIdAndPermissions();
        var principal = principalWithId(PermissionFixture.WRITER_ID);
        var context = contextWith(worldId, principal);

        when(worldRepository.findByPublicId(worldId)).thenReturn(Optional.of(world));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenRequesterHasNoWriteAccess() {

        // given
        var worldId = WorldFixture.PUBLIC_ID;
        var world = WorldFixture.privateWorldWithId();
        var principal = principalWithId(9999L);
        var context = contextWith(worldId, principal);

        when(worldRepository.findByPublicId(worldId)).thenReturn(Optional.of(world));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenWorldNotFound() {

        // given
        var worldId = UUID.randomUUID();
        var principal = principalWithId(1L);
        var context = contextWith(worldId, principal);

        when(worldRepository.findByPublicId(worldId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> authorizer.authorize(context))
                .isInstanceOf(AssetNotFoundException.class);
    }

    private MoiraiPrincipal principalWithId(Long id) {
        return new MoiraiPrincipal(
                UUID.randomUUID(),
                id,
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
