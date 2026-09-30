package me.moirai.storyengine.infrastructure.security.authorization.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.inbound.message.MessageAuthorship;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureAuthorizationReader;
import me.moirai.storyengine.core.port.outbound.message.MessageAuthorizationReader;

@ExtendWith(MockitoExtension.class)
public class EditMessageAuthorizerTest {

    private static final UUID ADVENTURE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CALLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID MESSAGE_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID OTHER_AUTHOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000009");

    @Mock
    private AdventureAuthorizationReader adventureAuthorizationReader;

    @Mock
    private MessageAuthorizationReader messageAuthorizationReader;

    @InjectMocks
    private EditMessageAuthorizer authorizer;

    @Test
    void shouldExposeTheEditMessageOperation() {

        // when
        var operation = authorizer.getOperation();

        // then
        assertThat(operation).isEqualTo(AuthorizationOperation.EDIT_MESSAGE);
    }

    @Test
    void shouldAuthorizeAPlayerEditingAnyMessageTheyAuthored() {

        // given
        givenPermissions(OWNER_ID, List.of());
        givenMessageAuthoredBy(CALLER_ID);

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldNotAuthorizeAPlayerEditingAMessageAuthoredBySomeoneElse() {

        // given
        givenPermissions(OWNER_ID, List.of());
        givenMessageAuthoredBy(OTHER_AUTHOR_ID);

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeAPlayerEditingANarratorMessage() {

        // given
        givenPermissions(OWNER_ID, List.of());
        givenMessageAuthoredBy(null);

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeAPlayerEditingAMessageThatDoesNotExist() {

        // given
        givenPermissions(OWNER_ID, List.of());

        when(messageAuthorizationReader.getMessageAuthor(any())).thenReturn(Optional.empty());

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldAuthorizeAManagerEditingANarratorMessage() {

        // given
        givenPermissions(CALLER_ID, List.of());

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldAuthorizeAWriterEditingAnyMessage() {

        // given
        givenPermissions(OWNER_ID, List.of(CALLER_ID));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldAuthorizeAnAdminEditingAnyMessage() {

        // given
        givenPermissions(OWNER_ID, List.of());

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.ADMIN)));

        // then
        assertThat(isAuthorized).isTrue();
    }

    private void givenPermissions(UUID ownerId, List<UUID> writers) {
        when(adventureAuthorizationReader.getAuthorizationData(any()))
                .thenReturn(Optional.of(new AssetPermissionsData(ownerId, writers, List.of(), Visibility.PRIVATE)));
    }

    private void givenMessageAuthoredBy(UUID authorId) {
        when(messageAuthorizationReader.getMessageAuthor(any()))
                .thenReturn(Optional.of(new MessageAuthorship(MESSAGE_ID, authorId)));
    }

    private MoiraiPrincipal principal(Role role) {
        return new MoiraiPrincipal(
                CALLER_ID, 1L, "caller", "token", "refresh", role, null);
    }

    private AuthorizationContext contextWith(MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("adventureId", ADVENTURE_ID, "messageId", MESSAGE_ID));
    }
}
