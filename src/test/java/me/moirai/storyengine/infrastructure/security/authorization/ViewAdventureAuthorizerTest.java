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
import me.moirai.storyengine.infrastructure.security.authorization.adventure.ViewAdventureAuthorizer;

@ExtendWith(MockitoExtension.class)
class ViewAdventureAuthorizerTest {

    @Mock
    private AdventureRepository adventureRepository;

    @InjectMocks
    private ViewAdventureAuthorizer authorizer;

    @Test
    void shouldReturnViewAdventureOperation() {

        assertThat(authorizer.getOperation()).isEqualTo(AuthorizationOperation.VIEW_ADVENTURE);
    }

    @Test
    void shouldAuthorizeWhenRequesterIsOwner() {

        // given
        var adventureId = AdventureFixture.PUBLIC_ID;
        var adventure = AdventureFixture.privateMultiplayerAdventureWithIdAndPermissions();
        var principal = principalWithId(PermissionFixture.OWNER_ID);
        var context = contextWith(adventureId, principal);

        when(adventureRepository.findByPublicId(adventureId)).thenReturn(Optional.of(adventure));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenRequesterIsAllowedToRead() {

        // given
        var adventureId = AdventureFixture.PUBLIC_ID;
        var adventure = AdventureFixture.privateMultiplayerAdventureWithIdAndPermissions();
        var principal = principalWithId(PermissionFixture.READER_ID);
        var context = contextWith(adventureId, principal);

        when(adventureRepository.findByPublicId(adventureId)).thenReturn(Optional.of(adventure));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenAdventureIsPublic() {

        // given
        var adventureId = AdventureFixture.PUBLIC_ID;
        var adventure = AdventureFixture.publicMultiplayerAdventureWithId();
        var principal = principalWithId(9999L);
        var context = contextWith(adventureId, principal);

        when(adventureRepository.findByPublicId(adventureId)).thenReturn(Optional.of(adventure));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenAdventureIsPrivateAndRequesterHasNoReadAccess() {

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
