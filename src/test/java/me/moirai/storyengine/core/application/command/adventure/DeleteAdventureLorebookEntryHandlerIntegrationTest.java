package me.moirai.storyengine.core.application.command.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import me.moirai.storyengine.AbstractDatabaseIntegrationTest;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authentication.MoiraiSecurityContext;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.port.outbound.adventure.LorebookVectorSearchPort;

public class DeleteAdventureLorebookEntryHandlerIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final String COUNT_ENTRIES_BY_PUBLIC_ID = """
            SELECT COUNT(*)
              FROM adventure_lorebook
             WHERE public_id = :publicId
            """;

    @Autowired
    private DeleteAdventureLorebookEntryHandler handler;

    @Autowired
    private LorebookVectorSearchPort vectorSearchPort;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @AfterEach
    public void after() {
        MoiraiSecurityContext.clear();
    }

    @Test
    public void shouldDeleteTheVectorAfterCommitWhenTheEntryIsRemoved() {

        // given
        var command = commandForNewEntry();

        // when
        handler.handle(command);

        // then
        assertThat(countEntries(command.entryId())).isZero();
        verify(vectorSearchPort).delete(command.entryId());
    }

    @Test
    public void shouldKeepTheEntryAndNotTouchTheVectorWhenTheTransactionRollsBack() {

        // given
        var command = commandForNewEntry();
        var outerTransaction = new TransactionTemplate(transactionManager);

        // when
        outerTransaction.executeWithoutResult(status -> {
            handler.handle(command);
            status.setRollbackOnly();
        });

        // then
        assertThat(countEntries(command.entryId())).isOne();
        verify(vectorSearchPort, never()).delete(any());
    }

    @Test
    public void shouldStillRemoveTheEntryWhenTheVectorDeleteFailsAfterCommit() {

        // given
        var command = commandForNewEntry();

        doThrow(new RuntimeException("qdrant down")).when(vectorSearchPort).delete(any());

        // then
        assertThatNoException().isThrownBy(() -> handler.handle(command));
        assertThat(countEntries(command.entryId())).isZero();
        verify(vectorSearchPort).delete(command.entryId());
    }

    private DeleteAdventureLorebookEntry commandForNewEntry() {

        var owner = insert(UserFixture.player().username("owner").discordId("lorebook-owner").build(), User.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);

        var adventure = AdventureFixture.privateAdventure()
                .worldId(world.getPublicId())
                .permissions(new Permission(owner.getId(), PermissionLevel.OWNER))
                .build();

        var entry = adventure.addLorebookEntry("Winterhold", "A ruined city");

        insert(adventure, Adventure.class);

        MoiraiSecurityContext.set(new MoiraiPrincipal(
                owner.getPublicId(), owner.getId(), "owner", "token", "refresh", Role.PLAYER, null));

        return new DeleteAdventureLorebookEntry(entry.getPublicId(), adventure.getPublicId());
    }

    private Long countEntries(UUID entryId) {

        return jdbcClient.sql(COUNT_ENTRIES_BY_PUBLIC_ID)
                .param("publicId", entryId)
                .query(Long.class)
                .single();
    }
}
