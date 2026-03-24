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
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.infrastructure.security.authorization.persona.UpdatePersonaAuthorizer;

@ExtendWith(MockitoExtension.class)
class UpdatePersonaAuthorizerTest {

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private UpdatePersonaAuthorizer authorizer;

    @Test
    void shouldReturnUpdatePersonaOperation() {

        assertThat(authorizer.getOperation()).isEqualTo(AuthorizationOperation.UPDATE_PERSONA);
    }

    @Test
    void shouldAuthorizeWhenRequesterIsOwner() {

        // given
        var personaId = PersonaFixture.PUBLIC_ID;
        var persona = PersonaFixture.publicPersonaWithIdAndPermissions();
        var principal = principalWithId(PermissionFixture.OWNER_ID);
        var context = contextWith(personaId, principal);

        when(personaRepository.findByPublicId(personaId)).thenReturn(Optional.of(persona));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenRequesterIsAllowedToWrite() {

        // given
        var personaId = PersonaFixture.PUBLIC_ID;
        var persona = PersonaFixture.publicPersonaWithIdAndPermissions();
        var principal = principalWithId(PermissionFixture.WRITER_ID);
        var context = contextWith(personaId, principal);

        when(personaRepository.findByPublicId(personaId)).thenReturn(Optional.of(persona));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenRequesterHasNoWriteAccess() {

        // given
        var personaId = PersonaFixture.PUBLIC_ID;
        var persona = PersonaFixture.privatePersonaWithId();
        var principal = principalWithId(9999L);
        var context = contextWith(personaId, principal);

        when(personaRepository.findByPublicId(personaId)).thenReturn(Optional.of(persona));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenPersonaNotFound() {

        // given
        var personaId = UUID.randomUUID();
        var principal = principalWithId(1L);
        var context = contextWith(personaId, principal);

        when(personaRepository.findByPublicId(personaId)).thenReturn(Optional.empty());

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

    private AuthorizationContext contextWith(UUID personaId, MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("personaId", personaId));
    }
}
