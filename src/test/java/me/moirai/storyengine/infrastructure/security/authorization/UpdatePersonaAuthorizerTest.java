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
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.persona.PersonaAuthorizationReader;
import me.moirai.storyengine.infrastructure.security.authorization.persona.UpdatePersonaAuthorizer;

@ExtendWith(MockitoExtension.class)
class UpdatePersonaAuthorizerTest {

    private static final UUID OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID WRITER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Mock
    private PersonaAuthorizationReader reader;

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
        var authData = personaWithPermissions();
        var principal = principalWithPublicId(OWNER_UUID);
        var context = contextWith(personaId, principal);

        when(reader.getAuthorizationData(personaId)).thenReturn(Optional.of(authData));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenRequesterIsAllowedToWrite() {

        // given
        var personaId = PersonaFixture.PUBLIC_ID;
        var authData = personaWithPermissions();
        var principal = principalWithPublicId(WRITER_UUID);
        var context = contextWith(personaId, principal);

        when(reader.getAuthorizationData(personaId)).thenReturn(Optional.of(authData));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenRequesterHasNoWriteAccess() {

        // given
        var personaId = PersonaFixture.PUBLIC_ID;
        var authData = personaNoPermissions();
        var principal = principalWithPublicId(UUID.randomUUID());
        var context = contextWith(personaId, principal);

        when(reader.getAuthorizationData(personaId)).thenReturn(Optional.of(authData));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenPersonaNotFound() {

        // given
        var personaId = UUID.randomUUID();
        var principal = principalWithPublicId(UUID.randomUUID());
        var context = contextWith(personaId, principal);

        when(reader.getAuthorizationData(personaId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> authorizer.authorize(context))
                .isInstanceOf(NotFoundException.class);
    }

    private AssetPermissionsData personaWithPermissions() {
        return new AssetPermissionsData(OWNER_UUID, List.of(WRITER_UUID), List.of(), Visibility.PUBLIC);
    }

    private AssetPermissionsData personaNoPermissions() {
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

    private AuthorizationContext contextWith(UUID personaId, MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("personaId", personaId));
    }
}
