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
import me.moirai.storyengine.core.port.outbound.adventure.AdventureReader;
import me.moirai.storyengine.core.port.outbound.message.MessageAuthorizationReader;

@ExtendWith(MockitoExtension.class)
public class RetryNarrationAuthorizerTest {

    private static final UUID ADVENTURE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CALLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID OTHER_AUTHOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000009");
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Mock
    private AdventureAuthorizationReader adventureAuthorizationReader;

    @Mock
    private AdventureReader adventureReader;

    @Mock
    private MessageAuthorizationReader messageAuthorizationReader;

    @InjectMocks
    private RetryNarrationAuthorizer authorizer;

    @Test
    void shouldExposeTheRetryNarrationOperation() {

        // when
        var operation = authorizer.getOperation();

        // then
        assertThat(operation).isEqualTo(AuthorizationOperation.RETRY_NARRATION);
    }

    @Test
    void shouldAuthorizeTheAuthorOfTheLastPlayerMessage() {

        // given
        givenCallerIsEnrolled();
        givenPermissions(OWNER_ID, List.of());
        givenLastPlayerMessageAuthoredBy(CALLER_ID);

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldNotAuthorizeWhenTheLastPlayerMessageBelongsToSomeoneElse() {

        // given
        givenCallerIsEnrolled();
        givenPermissions(OWNER_ID, List.of());
        givenLastPlayerMessageAuthoredBy(OTHER_AUTHOR_ID);

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeWhenThereAreNoPlayerMessages() {

        // given
        givenCallerIsEnrolled();
        givenPermissions(OWNER_ID, List.of());

        when(messageAuthorizationReader.getLastPlayerMessage(any())).thenReturn(Optional.empty());

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldAuthorizeTheOwnerRegardlessOfWhoSentTheLastPlayerMessage() {

        // given
        givenCallerIsEnrolled();
        givenPermissions(CALLER_ID, List.of());

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldAuthorizeAWriterRegardlessOfWhoSentTheLastPlayerMessage() {

        // given
        givenCallerIsEnrolled();
        givenPermissions(OWNER_ID, List.of(CALLER_ID));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER)));

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldNotAuthorizeAUserWithoutAnEnrolledCharacter() {

        // given
        when(adventureReader.getEnrolledPlayerIds(any())).thenReturn(List.of(OWNER_ID));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.ADMIN)));

        // then
        assertThat(isAuthorized).isFalse();
    }

    private void givenCallerIsEnrolled() {
        when(adventureReader.getEnrolledPlayerIds(any())).thenReturn(List.of(CALLER_ID));
    }

    private void givenPermissions(UUID ownerId, List<UUID> writers) {
        when(adventureAuthorizationReader.getAuthorizationData(any()))
                .thenReturn(Optional.of(new AssetPermissionsData(ownerId, writers, List.of(), Visibility.PRIVATE)));
    }

    private void givenLastPlayerMessageAuthoredBy(UUID authorId) {
        when(messageAuthorizationReader.getLastPlayerMessage(any()))
                .thenReturn(Optional.of(new MessageAuthorship(UUID.randomUUID(), authorId)));
    }

    private MoiraiPrincipal principal(Role role) {
        return new MoiraiPrincipal(
                CALLER_ID, 1L, "caller", "token", "refresh", role, null);
    }

    private AuthorizationContext contextWith(MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("adventureId", ADVENTURE_ID));
    }
}
