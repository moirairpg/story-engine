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
import me.moirai.storyengine.core.port.outbound.adventure.InvitationRecipientRow;
import me.moirai.storyengine.core.port.outbound.adventure.InvitationReader;

@ExtendWith(MockitoExtension.class)
public class DeclineAdventureInvitationAuthorizerTest {

    private static final UUID INVITATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private InvitationReader invitationReader;

    @InjectMocks
    private DeclineAdventureInvitationAuthorizer authorizer;

    @Test
    void shouldAuthorizeTheRecipientOfAPendingInvitation() {

        // given
        when(invitationReader.getByPublicId(any()))
                .thenReturn(Optional.of(new InvitationRecipientRow("caller", InvitationStatus.PENDING)));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER, "caller")));

        // then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    void shouldNotAuthorizeSomeoneWhoIsNotTheRecipient() {

        // given
        when(invitationReader.getByPublicId(any()))
                .thenReturn(Optional.of(new InvitationRecipientRow("someone.else", InvitationStatus.PENDING)));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER, "caller")));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeWhenInvitationIsAlreadyAnswered() {

        // given
        when(invitationReader.getByPublicId(any()))
                .thenReturn(Optional.of(new InvitationRecipientRow("caller", InvitationStatus.ACCEPTED)));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER, "caller")));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeWhenInvitationIsMissing() {

        // given
        when(invitationReader.getByPublicId(any())).thenReturn(Optional.empty());

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.PLAYER, "caller")));

        // then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    void shouldNotAuthorizeAnAdminWhoIsNotTheRecipient() {

        // given
        when(invitationReader.getByPublicId(any()))
                .thenReturn(Optional.of(new InvitationRecipientRow("someone.else", InvitationStatus.PENDING)));

        // when
        var isAuthorized = authorizer.authorize(contextWith(principal(Role.ADMIN, "caller")));

        // then
        assertThat(isAuthorized).isFalse();
    }

    private MoiraiPrincipal principal(Role role, String username) {
        return new MoiraiPrincipal(
                UUID.randomUUID(), 1L, username, "token", "refresh", role, null);
    }

    private AuthorizationContext contextWith(MoiraiPrincipal principal) {
        return new AuthorizationContext(principal, Map.of("invitationId", INVITATION_ID));
    }
}
