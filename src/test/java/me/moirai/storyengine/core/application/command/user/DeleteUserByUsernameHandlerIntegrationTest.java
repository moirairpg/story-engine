package me.moirai.storyengine.core.application.command.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import me.moirai.storyengine.AbstractDatabaseIntegrationTest;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.NotificationLevel;
import me.moirai.storyengine.common.enums.NotificationType;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authentication.MoiraiSecurityContext;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.ContextAttributesFixture;
import me.moirai.storyengine.core.domain.adventure.ModelConfigurationFixture;
import me.moirai.storyengine.core.domain.character.PlayerCharacter;
import me.moirai.storyengine.core.domain.character.PlayerCharacterFixture;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.userdetails.DeleteUserByUsername;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

public class DeleteUserByUsernameHandlerIntegrationTest extends AbstractDatabaseIntegrationTest {

    @Autowired
    private DeleteUserByUsernameHandler handler;

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private StoragePort storagePort;

    private User doomedUser;
    private User survivingUser;

    @BeforeEach
    public void before() {

        clearDatabase();

        doomedUser = insertUser("11111", "doomed.player");
        survivingUser = insertUser("22222", "surviving.player");

        MoiraiSecurityContext.set(admin());
    }

    @AfterEach
    public void after() {
        MoiraiSecurityContext.clear();
    }

    @Test
    public void shouldDeleteTheCharactersOfTheUserWhenTheUserIsDeleted() {

        // given
        insertCharacter(doomedUser, "Doomed Hero");
        insertCharacter(survivingUser, "Surviving Hero");

        // when
        handler.handle(new DeleteUserByUsername(doomedUser.getUsername()));

        // then
        assertThat(countWhere("player_character", "player_id", doomedUser.getId())).isZero();
        assertThat(countWhere("player_character", "player_id", survivingUser.getId())).isOne();
    }

    @Test
    public void shouldDeleteTheAdventuresOwnedByTheUserWhenTheUserIsDeleted() {

        // given
        var ownedAdventure = insertAdventureOwnedBy(doomedUser);
        var otherAdventure = insertAdventureOwnedBy(survivingUser);

        // when
        handler.handle(new DeleteUserByUsername(doomedUser.getUsername()));

        // then
        assertThat(countById("adventure", ownedAdventure.getId())).isZero();
        assertThat(countById("adventure", otherAdventure.getId())).isOne();
    }

    @Test
    public void shouldLeaveNoPermissionRowBehindWhenTheOwnedAdventureIsDeleted() {

        // given
        var ownedAdventure = insertAdventureOwnedBy(doomedUser);

        // when
        handler.handle(new DeleteUserByUsername(doomedUser.getUsername()));

        // then
        assertThat(countWhere("adventure_permissions", "adventure_id", ownedAdventure.getId())).isZero();
        assertThat(countWhere("adventure_permissions", "user_id", doomedUser.getId())).isZero();
    }

    @Test
    public void shouldRevokeTheUserFromAdventuresTheyDoNotOwnWhenTheUserIsDeleted() {

        // given
        var sharedAdventure = insertAdventureOwnedBy(survivingUser);
        grantPermission("adventure_permissions", "adventure_id", sharedAdventure.getId(), doomedUser.getId());

        // when
        handler.handle(new DeleteUserByUsername(doomedUser.getUsername()));

        // then
        assertThat(countById("adventure", sharedAdventure.getId())).isOne();
        assertThat(countWhere("adventure_permissions", "user_id", doomedUser.getId())).isZero();
        assertThat(countWhere("adventure_permissions", "user_id", survivingUser.getId())).isOne();
    }

    @Test
    public void shouldDeleteTheWorldsOwnedByTheUserWhenTheUserIsDeleted() {

        // given
        var ownedWorld = insertWorldOwnedBy(doomedUser);
        var otherWorld = insertWorldOwnedBy(survivingUser);

        // when
        handler.handle(new DeleteUserByUsername(doomedUser.getUsername()));

        // then
        assertThat(countById("world", ownedWorld.getId())).isZero();
        assertThat(countById("world", otherWorld.getId())).isOne();
        assertThat(countWhere("world_permissions", "user_id", doomedUser.getId())).isZero();
    }

    @Test
    public void shouldRemoveTheWorldImageFromStorageWhenTheDeletionCommits() {

        // given
        var ownedWorld = insertWorldOwnedBy(doomedUser);
        ownedWorld.updateImageKey("worlds/doomed.png");
        update(ownedWorld, ownedWorld.getId(), World.class);

        // when
        handler.handle(new DeleteUserByUsername(doomedUser.getUsername()));

        // then
        verify(storagePort).delete("worlds/doomed.png");
    }

    @Test
    public void shouldRevokeTheUserFromWorldsTheyDoNotOwnWhenTheUserIsDeleted() {

        // given
        var sharedWorld = insertWorldOwnedBy(survivingUser);
        grantPermission("world_permissions", "world_id", sharedWorld.getId(), doomedUser.getId());

        // when
        handler.handle(new DeleteUserByUsername(doomedUser.getUsername()));

        // then
        assertThat(countById("world", sharedWorld.getId())).isOne();
        assertThat(countWhere("world_permissions", "user_id", doomedUser.getId())).isZero();
    }

    @Test
    public void shouldDeleteTheNotificationWhenTheUserWasItsOnlyRecipient() {

        // given
        var notification = insertSystemNotificationFor(doomedUser.getId());

        // when
        handler.handle(new DeleteUserByUsername(doomedUser.getUsername()));

        // then
        assertThat(countById("notification", notification.getId())).isZero();
        assertThat(countWhere("notification_recipient", "user_id", doomedUser.getId())).isZero();
    }

    @Test
    public void shouldKeepTheNotificationWhenOtherRecipientsRemain() {

        // given
        var notification = insertSystemNotificationFor(doomedUser.getId(), survivingUser.getId());
        markAsRead(notification, doomedUser.getId());
        markAsRead(notification, survivingUser.getId());

        // when
        handler.handle(new DeleteUserByUsername(doomedUser.getUsername()));

        // then
        assertThat(countById("notification", notification.getId())).isOne();
        assertThat(countWhere("notification_recipient", "user_id", doomedUser.getId())).isZero();
        assertThat(countWhere("notification_recipient", "user_id", survivingUser.getId())).isOne();
        assertThat(countWhere("notification_read", "user_id", doomedUser.getId())).isZero();
        assertThat(countWhere("notification_read", "user_id", survivingUser.getId())).isOne();
    }

    @Test
    public void shouldLeaveNoRowReferencingTheUserInAnyTableWhenTheUserIsDeleted() {

        // given
        var doomedCharacter = insertCharacter(doomedUser, "Doomed Hero");
        insertAdventureOwnedBy(doomedUser);
        insertWorldOwnedBy(doomedUser);
        insertSystemNotificationFor(doomedUser.getId());

        var sharedAdventure = insertAdventureOwnedBy(survivingUser);
        grantPermission("adventure_permissions", "adventure_id", sharedAdventure.getId(), doomedUser.getId());
        enrol(sharedAdventure, doomedCharacter);
        invite(sharedAdventure, doomedUser.getId(), survivingUser.getId());
        invite(sharedAdventure, survivingUser.getId(), doomedUser.getId());

        var sharedWorld = insertWorldOwnedBy(survivingUser);
        grantPermission("world_permissions", "world_id", sharedWorld.getId(), doomedUser.getId());

        var sharedNotification = insertSystemNotificationFor(doomedUser.getId(), survivingUser.getId());
        markAsRead(sharedNotification, doomedUser.getId());

        // when
        handler.handle(new DeleteUserByUsername(doomedUser.getUsername()));

        // then
        assertThat(countById("adventure", sharedAdventure.getId())).isOne();
        assertThat(countWhere("moirai_user", "id", doomedUser.getId())).isZero();
        assertThat(countWhere("player_character", "player_id", doomedUser.getId())).isZero();
        assertThat(countWhere("adventure_permissions", "user_id", doomedUser.getId())).isZero();
        assertThat(countWhere("world_permissions", "user_id", doomedUser.getId())).isZero();
        assertThat(countWhere("adventure_membership", "player_id", doomedUser.getId())).isZero();
        assertThat(countWhere("adventure_invitation", "user_id", doomedUser.getId())).isZero();
        assertThat(countWhere("adventure_invitation", "inviter_id", doomedUser.getId())).isZero();
        assertThat(countWhere("notification_recipient", "user_id", doomedUser.getId())).isZero();
        assertThat(countWhere("notification_read", "user_id", doomedUser.getId())).isZero();
    }

    private MoiraiPrincipal admin() {
        return new MoiraiPrincipal(
                UUID.randomUUID(), 1L, "requesting.admin", "token", "refresh", Role.ADMIN, null);
    }

    private User insertUser(String discordId, String username) {

        return insert(UserFixture.player()
                .discordId(discordId)
                .username(username)
                .build(), User.class);
    }

    private PlayerCharacter insertCharacter(User owner, String name) {

        return insert(PlayerCharacterFixture.samplePlayerCharacter()
                .name(name)
                .playerId(owner.getId())
                .build(), PlayerCharacter.class);
    }

    private Adventure insertAdventureOwnedBy(User owner) {

        var builder = Adventure.builder();
        builder.name("Adventure of " + owner.getUsername());
        builder.description("This is an RPG world");
        builder.adventureStart("As you enter the city, people around you start looking at you.");
        builder.worldId(UUID.randomUUID());
        builder.moderation(Moderation.STRICT);
        builder.modelConfiguration(ModelConfigurationFixture.gpt4Mini());
        builder.contextAttributes(ContextAttributesFixture.sample());
        builder.visibility(Visibility.fromString("PRIVATE"));
        builder.permissions(new Permission(owner.getId(), PermissionLevel.OWNER));

        return insert(builder.build(), Adventure.class);
    }

    private World insertWorldOwnedBy(User owner) {

        var builder = World.builder();
        builder.name("World of " + owner.getUsername());
        builder.description("This is an RPG world");
        builder.adventureStart("As you enter the city, people around you start looking at you.");
        builder.visibility(Visibility.fromString("PRIVATE"));
        builder.permissions(new Permission(owner.getId(), PermissionLevel.OWNER));

        return insert(builder.build(), World.class);
    }

    private Notification insertSystemNotificationFor(Long... recipientUserIds) {

        var notification = insert(Notification.builder()
                .message("System message")
                .type(NotificationType.SYSTEM)
                .level(NotificationLevel.INFO)
                .recipientUserIds(List.of(recipientUserIds))
                .build(), Notification.class);

        for (var userId : recipientUserIds) {
            jdbcClient.sql("INSERT INTO notification_recipient (notification_id, user_id) VALUES (:id, :userId)")
                    .param("id", notification.getId())
                    .param("userId", userId)
                    .update();
        }

        return notification;
    }

    private void markAsRead(Notification notification, Long userId) {

        jdbcClient.sql("INSERT INTO notification_read (notification_id, user_id, read_date) VALUES (:id, :userId, NOW())")
                .param("id", notification.getId())
                .param("userId", userId)
                .update();
    }

    private void enrol(Adventure adventure, PlayerCharacter character) {

        jdbcClient.sql("""
                INSERT INTO adventure_membership (adventure_id, player_character_id, player_id)
                     VALUES (:adventureId, :playerCharacterId, :playerId)
                """)
                .param("adventureId", adventure.getId())
                .param("playerCharacterId", character.getId())
                .param("playerId", character.getPlayerId())
                .update();
    }

    private void invite(Adventure adventure, Long inviteeId, Long inviterId) {

        jdbcClient.sql("""
                INSERT INTO adventure_invitation (public_id, adventure_id, user_id, inviter_id, status)
                     VALUES (:publicId, :adventureId, :userId, :inviterId, 'PENDING')
                """)
                .param("publicId", UUID.randomUUID())
                .param("adventureId", adventure.getId())
                .param("userId", inviteeId)
                .param("inviterId", inviterId)
                .update();
    }

    private void grantPermission(String table, String assetColumn, Long assetId, Long userId) {

        jdbcClient.sql("INSERT INTO " + table + " (" + assetColumn + ", user_id, permission) "
                + "VALUES (:assetId, :userId, 'READ')")
                .param("assetId", assetId)
                .param("userId", userId)
                .update();
    }

    private int countWhere(String table, String column, Long value) {

        return jdbcClient.sql("SELECT COUNT(*) FROM " + table + " WHERE " + column + " = :value")
                .param("value", value)
                .query(Integer.class)
                .single();
    }

    private int countById(String table, Long id) {

        return countWhere(table, "id", id);
    }
}
