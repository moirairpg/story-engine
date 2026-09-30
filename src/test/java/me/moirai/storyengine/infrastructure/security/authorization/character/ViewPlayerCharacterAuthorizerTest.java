package me.moirai.storyengine.infrastructure.security.authorization.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
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

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterReader;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterPermissionsData;

@ExtendWith(MockitoExtension.class)
public class ViewPlayerCharacterAuthorizerTest {

    private static final UUID CHARACTER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CALLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID STRANGER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final String CALLER_USERNAME = "caller";
    private static final String OWNER_USERNAME = "character.owner";

    @Mock
    private PlayerCharacterReader reader;

    @InjectMocks
    private ViewPlayerCharacterAuthorizer authorizer;

    @Test
    void shouldAuthorizeWhenPrincipalIsAdmin() {

        // given
        var context = contextWith(principal(Role.ADMIN));

        // when
        var isAuthorized = authorizer.authorize(context);

        // then
        assertThat(isAuthorized).isTrue();
        verifyNoInteractions(reader);
    }

    @Test
    void shouldAuthorizeWhenPrincipalOwnsCharacterRegisteredNowhere() {

        // given
        var context = contextWith(principal(Role.PLAYER));

        when(reader.getPermissions(any(UUID.class)))
                .thenReturn(Optional.of(new PlayerCharacterPermissionsData(CALLER_USERNAME, List.of())));

        // when
        var isAuthorized = authorizer.authorize(context);

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldAuthorizeWhenPrincipalOwnsCharacterRegisteredInAdventuresTheyCannotSee() {

        // given
        var context = contextWith(principal(Role.PLAYER));

        when(reader.getPermissions(any(UUID.class)))
                .thenReturn(Optional.of(new PlayerCharacterPermissionsData(
                        CALLER_USERNAME,
                        List.of(privateAdventureOwnedBy(STRANGER_ID)))));

        // when
        var isAuthorized = authorizer.authorize(context);

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldAuthorizeWhenCharacterIsRegisteredInPublicAdventureAndCallerIsNoMember() {

        // given
        var context = contextWith(principal(Role.PLAYER));

        when(reader.getPermissions(any(UUID.class)))
                .thenReturn(Optional.of(new PlayerCharacterPermissionsData(
                        OWNER_USERNAME,
                        List.of(new AssetPermissionsData(
                                STRANGER_ID, List.of(), List.of(), Visibility.PUBLIC)))));

        // when
        var isAuthorized = authorizer.authorize(context);

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldAuthorizeWhenCallerHasReadPermissionOnPrivateAdventure() {

        // given
        var context = contextWith(principal(Role.PLAYER));

        when(reader.getPermissions(any(UUID.class)))
                .thenReturn(Optional.of(new PlayerCharacterPermissionsData(
                        OWNER_USERNAME,
                        List.of(new AssetPermissionsData(
                                STRANGER_ID, List.of(), List.of(CALLER_ID), Visibility.PRIVATE)))));

        // when
        var isAuthorized = authorizer.authorize(context);

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldAuthorizeWhenCallerHasWritePermissionOnPrivateAdventure() {

        // given
        var context = contextWith(principal(Role.PLAYER));

        when(reader.getPermissions(any(UUID.class)))
                .thenReturn(Optional.of(new PlayerCharacterPermissionsData(
                        OWNER_USERNAME,
                        List.of(new AssetPermissionsData(
                                STRANGER_ID, List.of(CALLER_ID), List.of(), Visibility.PRIVATE)))));

        // when
        var isAuthorized = authorizer.authorize(context);

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldAuthorizeWhenCallerOwnsPrivateAdventureButNotCharacter() {

        // given
        var context = contextWith(principal(Role.PLAYER));

        when(reader.getPermissions(any(UUID.class)))
                .thenReturn(Optional.of(new PlayerCharacterPermissionsData(
                        OWNER_USERNAME,
                        List.of(privateAdventureOwnedBy(CALLER_ID)))));

        // when
        var isAuthorized = authorizer.authorize(context);

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldAuthorizeWhenAnyOfTheRegisteredAdventuresAdmitsCaller() {

        // given
        var context = contextWith(principal(Role.PLAYER));

        when(reader.getPermissions(any(UUID.class)))
                .thenReturn(Optional.of(new PlayerCharacterPermissionsData(
                        OWNER_USERNAME,
                        List.of(
                                privateAdventureOwnedBy(STRANGER_ID),
                                new AssetPermissionsData(
                                        STRANGER_ID, List.of(), List.of(CALLER_ID), Visibility.PRIVATE)))));

        // when
        var isAuthorized = authorizer.authorize(context);

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldNotAuthorizeWhenCallerIsNotOwnerAndCharacterIsRegisteredNowhere() {

        // given
        var context = contextWith(principal(Role.PLAYER));

        when(reader.getPermissions(any(UUID.class)))
                .thenReturn(Optional.of(new PlayerCharacterPermissionsData(OWNER_USERNAME, List.of())));

        // when
        var isAuthorized = authorizer.authorize(context);

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeWhenNoPrivateAdventurePermissionMatchesCaller() {

        // given
        var context = contextWith(principal(Role.PLAYER));

        when(reader.getPermissions(any(UUID.class)))
                .thenReturn(Optional.of(new PlayerCharacterPermissionsData(
                        OWNER_USERNAME,
                        List.of(new AssetPermissionsData(
                                STRANGER_ID, List.of(STRANGER_ID), List.of(STRANGER_ID), Visibility.PRIVATE)))));

        // when
        var isAuthorized = authorizer.authorize(context);

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeWhenCharacterIsNotFound() {

        // given
        var context = contextWith(principal(Role.PLAYER));

        when(reader.getPermissions(any(UUID.class))).thenReturn(Optional.empty());

        // when
        var isAuthorized = authorizer.authorize(context);

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldAdmitOnlyTheOwnerWhenCharacterIsRegisteredNowhere() {

        // given
        var permissionsData = new PlayerCharacterPermissionsData(OWNER_USERNAME, List.of());

        when(reader.getPermissions(any(UUID.class))).thenReturn(Optional.of(permissionsData));

        // when
        var ownerIsAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER, OWNER_USERNAME)));
        var strangerIsAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER, CALLER_USERNAME)));

        // then
        assertThat(ownerIsAuthorized).isTrue();
        assertThat(strangerIsAuthorized).isFalse();
    }

    private AssetPermissionsData privateAdventureOwnedBy(UUID ownerId) {
        return new AssetPermissionsData(ownerId, List.of(), List.of(), Visibility.PRIVATE);
    }

    private MoiraiPrincipal principal(Role role) {
        return principal(role, CALLER_USERNAME);
    }

    private MoiraiPrincipal principal(Role role, String username) {
        return new MoiraiPrincipal(
                CALLER_ID,
                1L,
                username,
                "token",
                "refresh",
                role,
                null);
    }

    private AuthorizationContext contextWith(MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("characterId", CHARACTER_ID));
    }
}
