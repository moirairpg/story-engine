package me.moirai.storyengine.infrastructure.security.authorization.authorizer;

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
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.infrastructure.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.infrastructure.security.authorization.AuthorizationContext;
import me.moirai.storyengine.infrastructure.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.infrastructure.security.authorization.authorizer.persona.DeletePersonaAuthorizer;

@ExtendWith(MockitoExtension.class)
class DeletePersonaAuthorizerTest {

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private DeletePersonaAuthorizer authorizer;

    @Test
    void shouldReturnDeletePersonaOperation() {

        assertThat(authorizer.getOperation()).isEqualTo(AuthorizationOperation.DELETE_PERSONA);
    }

    @Test
    void shouldAuthorizeWhenRequesterIsOwner() {

        var personaId = PersonaFixture.PUBLIC_ID;
        var ownerId = "586678721356875";
        var persona = PersonaFixture.publicPersonaWithId();
        var principal = principalWithDiscordId(ownerId);
        var context = contextWith(personaId, principal);

        when(personaRepository.findByPublicId(personaId)).thenReturn(Optional.of(persona));

        var result = authorizer.authorize(context);

        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenRequesterIsAllowedToWrite() {

        var personaId = PersonaFixture.PUBLIC_ID;
        var writerId = "613226587696519";
        var persona = PersonaFixture.publicPersonaWithId();
        var principal = principalWithDiscordId(writerId);
        var context = contextWith(personaId, principal);

        when(personaRepository.findByPublicId(personaId)).thenReturn(Optional.of(persona));

        var result = authorizer.authorize(context);

        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenRequesterHasNoWriteAccess() {

        var personaId = PersonaFixture.PUBLIC_ID;
        var strangerId = "999999999999999";
        var persona = PersonaFixture.privatePersonaWithId();
        var principal = principalWithDiscordId(strangerId);
        var context = contextWith(personaId, principal);

        when(personaRepository.findByPublicId(personaId)).thenReturn(Optional.of(persona));

        var result = authorizer.authorize(context);

        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenPersonaNotFound() {

        var personaId = UUID.randomUUID();
        var principal = principalWithDiscordId("586678721356875");
        var context = contextWith(personaId, principal);

        when(personaRepository.findByPublicId(personaId)).thenReturn(Optional.empty());

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

    private AuthorizationContext contextWith(UUID personaId, MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("personaId", personaId));
    }
}
