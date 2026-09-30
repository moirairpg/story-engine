package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import jakarta.transaction.Transactional;
import me.moirai.storyengine.AbstractDatabaseIntegrationTest;
import me.moirai.storyengine.common.enums.InvitationStatus;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.outbound.adventure.InvitationReader;

@Transactional
public class InvitationReaderImplIntegrationTest extends AbstractDatabaseIntegrationTest {

    @Autowired
    private InvitationReader reader;

    @Autowired
    private JdbcClient jdbcClient;

    private World world;

    @BeforeEach
    public void before() {

        clearDatabase();
        world = insert(WorldFixture.publicWorld().build(), World.class);
    }

    @Test
    public void shouldReturnPendingInvitationForAdventureAndRecipient() {

        // given
        var recipient = insertUser("bob", "11111");
        var adventure = insertAdventureWith(recipient, "alice", InvitationStatus.PENDING);

        // when
        var result = reader.getPendingByAdventureAndRecipient(adventure.getPublicId(), "bob");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().inviterDisplayName()).isEqualTo("alice the Great");
        assertThat(result.get().recipientUsername()).isEqualTo("bob");
    }

    @Test
    public void shouldNotReturnInvitationWhenTheInviterNoLongerExists() {

        // given
        var recipient = insertUser("bob", "11111");
        var adventure = insertAdventureWith(recipient, "alice", InvitationStatus.PENDING);

        jdbcClient.sql("DELETE FROM moirai_user WHERE username = 'alice'").update();

        // when
        var result = reader.getPendingByAdventureAndRecipient(adventure.getPublicId(), "bob");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnEmptyForAnAnsweredInvitationByAdventureAndRecipient() {

        // given
        var recipient = insertUser("bob", "11111");
        var adventure = insertAdventureWith(recipient, "alice", InvitationStatus.ACCEPTED);

        // when
        var result = reader.getPendingByAdventureAndRecipient(adventure.getPublicId(), "bob");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnEmptyForAWrongRecipient() {

        // given
        var recipient = insertUser("bob", "11111");
        insertUser("carol", "22222");
        var adventure = insertAdventureWith(recipient, "alice", InvitationStatus.PENDING);

        // when
        var result = reader.getPendingByAdventureAndRecipient(adventure.getPublicId(), "carol");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnPendingInvitationByPublicId() {

        // given
        var recipient = insertUser("bob", "11111");
        var adventure = insertAdventureWith(recipient, "alice", InvitationStatus.PENDING);
        var invitationId = adventure.getInvitations().getFirst().getPublicId();

        // when
        var result = reader.getPendingByPublicId(invitationId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().adventureName()).isEqualTo(adventure.getName());
        assertThat(result.get().inviterDisplayName()).isEqualTo("alice the Great");
    }

    @Test
    public void shouldReturnRecipientAndStatusByPublicIdForAnAnsweredInvitation() {

        // given
        var recipient = insertUser("bob", "11111");
        var adventure = insertAdventureWith(recipient, "alice", InvitationStatus.DECLINED);
        var invitationId = adventure.getInvitations().getFirst().getPublicId();

        // when
        var result = reader.getByPublicId(invitationId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().recipientUsername()).isEqualTo("bob");
        assertThat(result.get().status()).isEqualTo(InvitationStatus.DECLINED);
    }

    @Test
    public void shouldReturnEmptyByPublicIdWhenUnknown() {

        // when
        var result = reader.getByPublicId(UUID.randomUUID());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnAllPendingInvitationsForRecipientExcludingAnswered() {

        // given
        var recipient = insertUser("bob", "11111");
        insertAdventureWith(recipient, "alice", InvitationStatus.PENDING);
        insertAdventureWith(recipient, "carol", InvitationStatus.PENDING);
        insertAdventureWith(recipient, "dave", InvitationStatus.ACCEPTED);

        // when
        var result = reader.getAllPendingByRecipient("bob");

        // then
        assertThat(result).hasSize(2)
                .extracting(row -> row.inviterDisplayName())
                .containsExactlyInAnyOrder("alice the Great", "carol the Great");
    }

    @Test
    public void shouldReturnEmptyListWhenRecipientHasNoPendingInvitations() {

        // given
        insertUser("bob", "11111");

        // when
        var result = reader.getAllPendingByRecipient("bob");

        // then
        assertThat(result).isEmpty();
    }

    private User insertUser(String username, String discordId) {

        return insert(UserFixture.player()
                .username(username)
                .displayName(username + " the Great")
                .discordId(discordId)
                .build(), User.class);
    }

    private Adventure insertAdventureWith(User recipient, String inviterUsername, InvitationStatus status) {

        var inviter = insertUser(inviterUsername, "inviter-" + inviterUsername);

        var adventure = insert(AdventureFixture.publicAdventure()
                .worldId(world.getPublicId())
                .build(), Adventure.class);

        var invitation = adventure.invite(recipient.getId(), inviter.getId());
        invitation.setCreationDate(Instant.now());

        if (status == InvitationStatus.ACCEPTED) {
            invitation.accept();
        } else if (status == InvitationStatus.DECLINED) {
            invitation.decline();
        }

        update(adventure, adventure.getId(), Adventure.class);

        return adventure;
    }
}
