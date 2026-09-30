package me.moirai.storyengine.core.application.query.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.GetPendingAdventureInvitation;
import me.moirai.storyengine.core.port.outbound.adventure.InvitationReader;
import me.moirai.storyengine.core.port.outbound.adventure.PendingInvitationRow;

@ExtendWith(MockitoExtension.class)
public class GetPendingAdventureInvitationHandlerTest {

    @Mock
    private InvitationReader invitationReader;

    @InjectMocks
    private GetPendingAdventureInvitationHandler handler;

    @Test
    public void shouldReturnDetailsWhenAPendingInvitationExists() {

        // given
        var invitationId = UUID.randomUUID();
        var adventureId = UUID.randomUUID();
        var query = new GetPendingAdventureInvitation(adventureId, "some_user");

        when(invitationReader.getPendingByAdventureAndRecipient(any(), anyString()))
                .thenReturn(Optional.of(new PendingInvitationRow(
                        invitationId, adventureId, "Dragon Hunt", "Alice Liddell", "some_user", Instant.now())));

        // when
        var result = handler.execute(query);

        // then
        assertThat(result.invitationId()).isEqualTo(invitationId);
        assertThat(result.adventureName()).isEqualTo("Dragon Hunt");
        assertThat(result.inviterDisplayName()).isEqualTo("Alice Liddell");
    }

    @Test
    public void shouldThrowWhenNoPendingInvitationExists() {

        // given
        var query = new GetPendingAdventureInvitation(UUID.randomUUID(), "some_user");

        when(invitationReader.getPendingByAdventureAndRecipient(any(), anyString()))
                .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> handler.execute(query)).isInstanceOf(NotFoundException.class);
    }
}
