package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.InvitationStatus;
import me.moirai.storyengine.core.port.outbound.adventure.InvitationReader;
import me.moirai.storyengine.core.port.outbound.adventure.InvitationRecipientRow;
import me.moirai.storyengine.core.port.outbound.adventure.PendingInvitationRow;

@Repository
public class InvitationReaderImpl implements InvitationReader {

    //@formatter:off
    private static final String SELECT_PENDING_BY_ADVENTURE_AND_RECIPIENT = """
            SELECT ai.public_id         AS invitation_id,
                   a.public_id          AS adventure_id,
                   a.name               AS adventure_name,
                   inviter.display_name AS inviter_display_name,
                   u.username           AS recipient_username,
                   ai.creation_date     AS creation_date
              FROM adventure_invitation ai
                   JOIN adventure a         ON a.id       = ai.adventure_id
                   JOIN moirai_user u       ON u.id       = ai.user_id
                   JOIN moirai_user inviter ON inviter.id = ai.inviter_id
             WHERE a.public_id = :adventurePublicId
               AND u.username  = :recipientUsername
               AND ai.status   = 'PENDING'
            """;

    private static final String SELECT_PENDING_BY_PUBLIC_ID = """
            SELECT ai.public_id         AS invitation_id,
                   a.public_id          AS adventure_id,
                   a.name               AS adventure_name,
                   inviter.display_name AS inviter_display_name,
                   u.username           AS recipient_username,
                   ai.creation_date     AS creation_date
              FROM adventure_invitation ai
                   JOIN adventure a         ON a.id       = ai.adventure_id
                   JOIN moirai_user u       ON u.id       = ai.user_id
                   JOIN moirai_user inviter ON inviter.id = ai.inviter_id
             WHERE ai.public_id = :invitationId
               AND ai.status    = 'PENDING'
            """;

    private static final String SELECT_RECIPIENT_BY_PUBLIC_ID = """
            SELECT u.username AS recipient_username,
                   ai.status  AS status
              FROM adventure_invitation ai
                   JOIN moirai_user u ON u.id = ai.user_id
             WHERE ai.public_id = :invitationId
            """;

    private static final String SELECT_ALL_PENDING_BY_RECIPIENT = """
            SELECT ai.public_id         AS invitation_id,
                   a.public_id          AS adventure_id,
                   a.name               AS adventure_name,
                   inviter.display_name AS inviter_display_name,
                   u.username           AS recipient_username,
                   ai.creation_date     AS creation_date
              FROM adventure_invitation ai
                   JOIN adventure a         ON a.id       = ai.adventure_id
                   JOIN moirai_user u       ON u.id       = ai.user_id
                   JOIN moirai_user inviter ON inviter.id = ai.inviter_id
             WHERE u.username = :recipientUsername
               AND ai.status  = 'PENDING'
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public InvitationReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<PendingInvitationRow> getPendingByAdventureAndRecipient(
            UUID adventurePublicId,
            String recipientUsername) {

        return jdbcClient.sql(SELECT_PENDING_BY_ADVENTURE_AND_RECIPIENT)
                .param("adventurePublicId", adventurePublicId)
                .param("recipientUsername", recipientUsername)
                .query(toPendingInvitationRow())
                .optional();
    }

    @Override
    public Optional<PendingInvitationRow> getPendingByPublicId(UUID invitationId) {

        return jdbcClient.sql(SELECT_PENDING_BY_PUBLIC_ID)
                .param("invitationId", invitationId)
                .query(toPendingInvitationRow())
                .optional();
    }

    @Override
    public Optional<InvitationRecipientRow> getByPublicId(UUID invitationId) {

        return jdbcClient.sql(SELECT_RECIPIENT_BY_PUBLIC_ID)
                .param("invitationId", invitationId)
                .query(toInvitationRecipientRow())
                .optional();
    }

    @Override
    public List<PendingInvitationRow> getAllPendingByRecipient(String recipientUsername) {

        return jdbcClient.sql(SELECT_ALL_PENDING_BY_RECIPIENT)
                .param("recipientUsername", recipientUsername)
                .query(toPendingInvitationRow())
                .list();
    }

    private RowMapper<PendingInvitationRow> toPendingInvitationRow() {

        return (rs, _) -> new PendingInvitationRow(
                rs.getObject("invitation_id", UUID.class),
                rs.getObject("adventure_id", UUID.class),
                rs.getString("adventure_name"),
                rs.getString("inviter_display_name"),
                rs.getString("recipient_username"),
                rs.getTimestamp("creation_date").toInstant());
    }

    private RowMapper<InvitationRecipientRow> toInvitationRecipientRow() {

        return (rs, _) -> new InvitationRecipientRow(
                rs.getString("recipient_username"),
                InvitationStatus.valueOf(rs.getString("status")));
    }
}
