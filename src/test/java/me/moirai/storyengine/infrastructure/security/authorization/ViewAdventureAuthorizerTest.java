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

        var adventureId = AdventureFixture.PUBLIC_ID;
        var ownerId = "586678721356875";
        var adventure = AdventureFixture.publicMultiplayerAdventureWithId();
        var principal = principalWithDiscordId(ownerId);
        var context = contextWith(adventureId, principal);

        when(adventureRepository.findByPublicId(adventureId)).thenReturn(Optional.of(adventure));

        var result = authorizer.authorize(context);

        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenRequesterIsAllowedToRead() {

        var adventureId = AdventureFixture.PUBLIC_ID;
        var readerId = "613226587696519";
        var adventure = AdventureFixture.privateMultiplayerAdventureWithId();
        var principal = principalWithDiscordId(readerId);
        var context = contextWith(adventureId, principal);

        when(adventureRepository.findByPublicId(adventureId)).thenReturn(Optional.of(adventure));

        var result = authorizer.authorize(context);

        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenAdventureIsPublic() {

        var adventureId = AdventureFixture.PUBLIC_ID;
        var strangerId = "999999999999999";
        var adventure = AdventureFixture.publicMultiplayerAdventureWithId();
        var principal = principalWithDiscordId(strangerId);
        var context = contextWith(adventureId, principal);

        when(adventureRepository.findByPublicId(adventureId)).thenReturn(Optional.of(adventure));

        var result = authorizer.authorize(context);

        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenAdventureIsPrivateAndRequesterHasNoReadAccess() {

        var adventureId = AdventureFixture.PUBLIC_ID;
        var strangerId = "999999999999999";
        var adventure = AdventureFixture.privateMultiplayerAdventureWithId();
        var principal = principalWithDiscordId(strangerId);
        var context = contextWith(adventureId, principal);

        when(adventureRepository.findByPublicId(adventureId)).thenReturn(Optional.of(adventure));

        var result = authorizer.authorize(context);

        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenAdventureNotFound() {

        var adventureId = UUID.randomUUID();
        var principal = principalWithDiscordId("586678721356875");
        var context = contextWith(adventureId, principal);

        when(adventureRepository.findByPublicId(adventureId)).thenReturn(Optional.empty());

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

    private AuthorizationContext contextWith(UUID adventureId, MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("adventureId", adventureId));
    }
}
