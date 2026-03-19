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
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import me.moirai.storyengine.infrastructure.security.authorization.world.UpdateWorldAuthorizer;

@ExtendWith(MockitoExtension.class)
class UpdateWorldAuthorizerTest {

    @Mock
    private WorldRepository worldRepository;

    @InjectMocks
    private UpdateWorldAuthorizer authorizer;

    @Test
    void shouldReturnUpdateWorldOperation() {

        assertThat(authorizer.getOperation()).isEqualTo(AuthorizationOperation.UPDATE_WORLD);
    }

    @Test
    void shouldAuthorizeWhenRequesterIsOwner() {

        var worldId = WorldFixture.PUBLIC_ID;
        var ownerId = "586678721356875";
        var world = WorldFixture.publicWorldWithId();
        var principal = principalWithDiscordId(ownerId);
        var context = contextWith(worldId, principal);

        when(worldRepository.findByPublicId(worldId)).thenReturn(Optional.of(world));

        var result = authorizer.authorize(context);

        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenRequesterIsAllowedToWrite() {

        var worldId = WorldFixture.PUBLIC_ID;
        var writerId = "613226587696519";
        var world = WorldFixture.publicWorldWithId();
        var principal = principalWithDiscordId(writerId);
        var context = contextWith(worldId, principal);

        when(worldRepository.findByPublicId(worldId)).thenReturn(Optional.of(world));

        var result = authorizer.authorize(context);

        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenRequesterHasNoWriteAccess() {

        var worldId = WorldFixture.PUBLIC_ID;
        var strangerId = "999999999999999";
        var world = WorldFixture.privateWorldWithId();
        var principal = principalWithDiscordId(strangerId);
        var context = contextWith(worldId, principal);

        when(worldRepository.findByPublicId(worldId)).thenReturn(Optional.of(world));

        var result = authorizer.authorize(context);

        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenWorldNotFound() {

        var worldId = UUID.randomUUID();
        var principal = principalWithDiscordId("586678721356875");
        var context = contextWith(worldId, principal);

        when(worldRepository.findByPublicId(worldId)).thenReturn(Optional.empty());

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

    private AuthorizationContext contextWith(UUID worldId, MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("worldId", worldId));
    }
}
