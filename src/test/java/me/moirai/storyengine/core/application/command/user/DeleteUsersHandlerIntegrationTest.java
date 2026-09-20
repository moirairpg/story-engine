package me.moirai.storyengine.core.application.command.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import me.moirai.storyengine.AbstractDatabaseIntegrationTest;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.userdetails.DeleteUsers;

public class DeleteUsersHandlerIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final UUID REQUESTER_ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

    @Autowired
    private DeleteUsersHandler handler;

    @Autowired
    private JdbcClient jdbcClient;

    private User firstUser;
    private User secondUser;

    @BeforeEach
    public void before() {

        clearDatabase();

        firstUser = insertUser("11111", "first.player");
        secondUser = insertUser("22222", "second.player");
    }

    @Test
    public void shouldCommitEveryAccountIndependentlyWhenOneAccountInTheSelectionFails() {

        // given
        var unknownUserId = UUID.randomUUID();

        var command = new DeleteUsers(
                List.of(firstUser.getPublicId(), unknownUserId, secondUser.getPublicId()), REQUESTER_ID);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUserIds()).containsExactly(unknownUserId);

        assertThat(countWhere("moirai_user", "id", firstUser.getId())).isZero();
        assertThat(countWhere("moirai_user", "id", secondUser.getId())).isZero();
    }

    @Test
    public void shouldDeleteTheOwnedAssetsOfEveryDeletedAccountWhenTheSelectionIsDeleted() {

        // given
        var firstWorld = insertWorldOwnedBy(firstUser);
        var secondWorld = insertWorldOwnedBy(secondUser);

        var command = new DeleteUsers(
                List.of(firstUser.getPublicId(), secondUser.getPublicId()), REQUESTER_ID);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUserIds()).isEmpty();

        assertThat(countWhere("world", "id", firstWorld.getId())).isZero();
        assertThat(countWhere("world", "id", secondWorld.getId())).isZero();
        assertThat(countWhere("world_permissions", "user_id", firstUser.getId())).isZero();
        assertThat(countWhere("world_permissions", "user_id", secondUser.getId())).isZero();
    }

    @Test
    public void shouldLeaveTheWholeSelectionIntactWhenTheRequesterIsInIt() {

        // given
        var command = new DeleteUsers(
                List.of(firstUser.getPublicId(), secondUser.getPublicId()), secondUser.getPublicId());

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BusinessRuleViolationException.class);

        assertThat(countWhere("moirai_user", "id", firstUser.getId())).isOne();
        assertThat(countWhere("moirai_user", "id", secondUser.getId())).isOne();
    }

    @Test
    public void shouldDeleteNothingWhenTheSelectionIsEmpty() {

        // given
        var command = new DeleteUsers(List.of(), REQUESTER_ID);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUserIds()).isEmpty();

        assertThat(countWhere("moirai_user", "id", firstUser.getId())).isOne();
        assertThat(countWhere("moirai_user", "id", secondUser.getId())).isOne();
    }

    private User insertUser(String discordId, String username) {

        return insert(UserFixture.player()
                .discordId(discordId)
                .username(username)
                .build(), User.class);
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

    private int countWhere(String table, String column, Long value) {

        return jdbcClient.sql("SELECT COUNT(*) FROM " + table + " WHERE " + column + " = :value")
                .param("value", value)
                .query(Integer.class)
                .single();
    }
}
