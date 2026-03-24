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
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.infrastructure.security.authorization.adventure.UpdateAdventureAuthorizer;

@ExtendWith(MockitoExtension.class)
class UpdateAdventureAuthorizerTest {

    @Mock
    private AdventureRepository adventureRepository;

    @InjectMocks
    private UpdateAdventureAuthorizer authorizer;

    @Test
    void shouldReturnUpdateAdventureOperation() {

        assertThat(authorizer.getOperation()).isEqualTo(AuthorizationOperation.UPDATE_ADVENTURE);
    }

    @Test
    void shouldAuthorizeWhenRequesterIsOwner() {

        // given
        var adventureId = AdventureFixture.PUBLIC_ID;
        var adventure = AdventureFixture.publicMultiplayerAdventureWithIdAndPermissions();
        var principal = principalWithId(PermissionFixture.OWNER_ID);
        var context = contextWith(adventureId, principal);

        when(adventureRepository.findByPublicId(adventureId)).thenReturn(Optional.of(adventure));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenRequesterIsAllowedToWrite() {

        // given
        var adventureId = AdventureFixture.PUBLIC_ID;
        var adventure = AdventureFixture.publicMultiplayerAdventureWithIdAndPermissions();
        var principal = principalWithId(PermissionFixture.WRITER_ID);
        var context = contextWith(adventureId, principal);

        when(adventureRepository.findByPublicId(adventureId)).thenReturn(Optional.of(adventure));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenRequesterHasNoWriteAccess() {

        // given
        var adventureId = AdventureFixture.PUBLIC_ID;
        var adventure = AdventureFixture.privateMultiplayerAdventureWithId();
        var principal = principalWithId(9999L);
        var context = contextWith(adventureId, principal);

        when(adventureRepository.findByPublicId(adventureId)).thenReturn(Optional.of(adventure));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenAdventureNotFound() {

        // given
        var adventureId = UUID.randomUUID();
        var principal = principalWithId(1L);
        var context = contextWith(adventureId, principal);

        when(adventureRepository.findByPublicId(adventureId)).thenReturn(Optional.empty());

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

    private AuthorizationContext contextWith(UUID adventureId, MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("adventureId", adventureId));
    }
}
