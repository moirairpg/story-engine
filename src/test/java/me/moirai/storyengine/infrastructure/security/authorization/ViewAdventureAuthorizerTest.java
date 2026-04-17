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
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureAuthorizationReader;
import me.moirai.storyengine.infrastructure.security.authorization.adventure.ViewAdventureAuthorizer;

@ExtendWith(MockitoExtension.class)
class ViewAdventureAuthorizerTest {

    private static final UUID OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID WRITER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID READER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Mock
    private AdventureAuthorizationReader reader;

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
        var authData = privateAdventureWithPermissions();
        var principal = principalWithPublicId(OWNER_UUID);
        var context = contextWith(adventureId, principal);

        when(reader.getAuthorizationData(adventureId)).thenReturn(Optional.of(authData));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenRequesterIsAllowedToRead() {

        // given
        var adventureId = AdventureFixture.PUBLIC_ID;
        var authData = privateAdventureWithPermissions();
        var principal = principalWithPublicId(READER_UUID);
        var context = contextWith(adventureId, principal);

        when(reader.getAuthorizationData(adventureId)).thenReturn(Optional.of(authData));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAuthorizeWhenAdventureIsPublic() {

        // given
        var adventureId = AdventureFixture.PUBLIC_ID;
        var authData = publicAdventureWithPermissions();
        var principal = principalWithPublicId(UUID.randomUUID());
        var context = contextWith(adventureId, principal);

        when(reader.getAuthorizationData(adventureId)).thenReturn(Optional.of(authData));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyWhenAdventureIsPrivateAndRequesterHasNoReadAccess() {

        // given
        var adventureId = AdventureFixture.PUBLIC_ID;
        var authData = privateAdventureNoPermissions();
        var principal = principalWithPublicId(UUID.randomUUID());
        var context = contextWith(adventureId, principal);

        when(reader.getAuthorizationData(adventureId)).thenReturn(Optional.of(authData));

        // when
        var result = authorizer.authorize(context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenAdventureNotFound() {

        // given
        var adventureId = UUID.randomUUID();
        var principal = principalWithPublicId(UUID.randomUUID());
        var context = contextWith(adventureId, principal);

        when(reader.getAuthorizationData(adventureId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> authorizer.authorize(context))
                .isInstanceOf(NotFoundException.class);
    }

    private AssetPermissionsData privateAdventureWithPermissions() {
        return new AssetPermissionsData(OWNER_UUID, List.of(WRITER_UUID), List.of(READER_UUID), Visibility.PRIVATE);
    }

    private AssetPermissionsData publicAdventureWithPermissions() {
        return new AssetPermissionsData(OWNER_UUID, List.of(WRITER_UUID), List.of(READER_UUID), Visibility.PUBLIC);
    }

    private AssetPermissionsData privateAdventureNoPermissions() {
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

    private AuthorizationContext contextWith(UUID adventureId, MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("adventureId", adventureId));
    }
}
