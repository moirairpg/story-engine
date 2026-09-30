package me.moirai.storyengine.infrastructure.security.authorization.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.InvitationStatus;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.core.port.outbound.adventure.InvitationReader;
import me.moirai.storyengine.core.port.outbound.adventure.InvitationRecipientRow;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterReader;

@ExtendWith(MockitoExtension.class)
public class JoinAdventureWithCharacterAuthorizerTest {

    private static final UUID INVITATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CHARACTER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Mock
    private InvitationReader invitationReader;

    @Mock
    private PlayerCharacterReader playerCharacterReader;

    @InjectMocks
    private JoinAdventureWithCharacterAuthorizer authorizer;

    @Test
    void shouldAuthorizePendingRecipientJoiningWithTheirOwnCharacter() {

        // given
        when(invitationReader.getByPublicId(any()))
                .thenReturn(Optional.of(new InvitationRecipientRow("caller", InvitationStatus.PENDING)));
        when(playerCharacterReader.getOwnerUsername(any())).thenReturn(Optional.of("caller"));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal("caller")));

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldNotAuthorizeSomeoneWhoIsNotTheRecipient() {

        // given
        when(invitationReader.getByPublicId(any()))
                .thenReturn(Optional.of(new InvitationRecipientRow("someone.else", InvitationStatus.PENDING)));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal("caller")));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeWhenInvitationIsAlreadyAnswered() {

        // given
        when(invitationReader.getByPublicId(any()))
                .thenReturn(Optional.of(new InvitationRecipientRow("caller", InvitationStatus.ACCEPTED)));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal("caller")));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeWhenInvitationIsMissing() {

        // given
        when(invitationReader.getByPublicId(any())).thenReturn(Optional.empty());

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal("caller")));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeJoiningWithACharacterTheCallerDoesNotOwn() {

        // given
        when(invitationReader.getByPublicId(any()))
                .thenReturn(Optional.of(new InvitationRecipientRow("caller", InvitationStatus.PENDING)));
        when(playerCharacterReader.getOwnerUsername(any())).thenReturn(Optional.of("someone.else"));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal("caller")));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeWhenCharacterIsMissing() {

        // given
        when(invitationReader.getByPublicId(any()))
                .thenReturn(Optional.of(new InvitationRecipientRow("caller", InvitationStatus.PENDING)));
        when(playerCharacterReader.getOwnerUsername(any())).thenReturn(Optional.empty());

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal("caller")));

        // then
        assertThat(isAuthorized).isFalse();
    }

    private MoiraiPrincipal principal(String username) {
        return new MoiraiPrincipal(
                UUID.randomUUID(), 1L, username, "token", "refresh", Role.PLAYER, null);
    }

    private AuthorizationContext contextWith(MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("invitationId", INVITATION_ID, "playerCharacterId", CHARACTER_ID));
    }
}
