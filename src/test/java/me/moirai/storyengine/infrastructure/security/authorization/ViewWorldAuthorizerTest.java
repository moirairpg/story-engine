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
import me.moirai.storyengine.infrastructure.security.authorization.world.ViewWorldAuthorizer;

@ExtendWith(MockitoExtension.class)
class ViewWorldAuthorizerTest {

    @Mock
    private WorldRepository worldRepository;

    @InjectMocks
    private ViewWorldAuthorizer authorizer;

    @Test
    void shouldReturnViewWorldOperation() {

        assertThat(authorizer.getOperation()).isEqualTo(AuthorizationOperation.VIEW_WORLD);
    }

    @Test
    void shouldAuthorizeWhenRequesterIsOwner() {

        // given
        var worldId = WorldFixture.PUBLIC_ID;
        var world = WorldFixture.privateWorldWithIdAndPermissions();
        var principal = principalWithId(PermissionFixture.OWNER_ID);
        var context = contextWith(worldId, principal);

        when(worldRepository.findByPublicId(worldId)).thenReturn(Optional.of(world));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenRequesterIsAllowedToRead() {

        // given
        var worldId = WorldFixture.PUBLIC_ID;
        var world = WorldFixture.privateWorldWithIdAndPermissions();
        var principal = principalWithId(PermissionFixture.READER_ID);
        var context = contextWith(worldId, principal);

        when(worldRepository.findByPublicId(worldId)).thenReturn(Optional.of(world));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenWorldIsPublic() {

        // given
        var worldId = WorldFixture.PUBLIC_ID;
        var world = WorldFixture.publicWorldWithId();
        var principal = principalWithId(9999L);
        var context = contextWith(worldId, principal);

        when(worldRepository.findByPublicId(worldId)).thenReturn(Optional.of(world));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenWorldIsPrivateAndRequesterHasNoReadAccess() {

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
