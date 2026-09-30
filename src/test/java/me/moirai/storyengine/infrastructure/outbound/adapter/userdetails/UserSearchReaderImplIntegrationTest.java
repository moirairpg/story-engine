package me.moirai.storyengine.infrastructure.outbound.adapter.userdetails;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractDatabaseIntegrationTest;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.enums.SortDirection;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.port.inbound.userdetails.SearchUsers;
import me.moirai.storyengine.core.port.inbound.userdetails.UserSortField;
import me.moirai.storyengine.core.port.outbound.userdetails.UserSearchReader;

public class UserSearchReaderImplIntegrationTest extends AbstractDatabaseIntegrationTest {

    @Autowired
    private UserSearchReader reader;

    @BeforeEach
    public void before() {
        clear(User.class);
    }

    @Test
    public void search_whenNoFilters_thenReturnAllUsers() {

        // given
        insert(userWith("alice", "1001", Role.PLAYER), User.class);
        insert(userWith("bob", "1002", Role.ADMIN), User.class);

        var query = new SearchUsers(null, null, null, null, null, null, null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(2);
    }

    @Test
    public void search_whenFilteredByPartialUsernameInAnotherCase_thenReturnMatchingOnly() {

        // given
        insert(userWith("Alice.Cooper", "1001", Role.PLAYER), User.class);
        insert(userWith("bob", "1002", Role.PLAYER), User.class);

        var query = new SearchUsers("LICE", null, null, null, null, null, null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(1);
        assertThat(result.data()).singleElement()
                .extracting("username").isEqualTo("Alice.Cooper");
    }

    @Test
    public void search_whenFilteredByRole_thenReturnMatchingOnly() {

        // given
        insert(userWith("alice", "1001", Role.PLAYER), User.class);
        insert(userWith("bob", "1002", Role.ADMIN), User.class);

        var query = new SearchUsers(null, null, Role.ADMIN, null, null, null, null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(1);
        assertThat(result.data()).singleElement()
                .extracting("role").isEqualTo(Role.ADMIN);
    }

    @Test
    public void search_whenFilteredByActiveState_thenReturnMatchingOnly() {

        // given
        insert(userWith("alice", "1001", Role.PLAYER), User.class);
        insert(deactivated(userWith("bob", "1002", Role.PLAYER)), User.class);

        var query = new SearchUsers(null, null, null, false, null, null, null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(1);
        assertThat(result.data()).singleElement()
                .extracting("username").isEqualTo("bob");
    }

    @Test
    public void search_whenFilteredByRegistrationRange_thenExcludeUsersOutsideIt() {

        // given
        var oldUser = insert(userWith("alice", "1001", Role.PLAYER), User.class);
        insert(userWith("bob", "1002", Role.PLAYER), User.class);

        backdateCreationDate(oldUser, Instant.now().minus(30, ChronoUnit.DAYS));

        var query = new SearchUsers(null, null, null, null, Instant.now().minus(1, ChronoUnit.DAYS), null,
                null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(1);
        assertThat(result.data()).singleElement()
                .extracting("username").isEqualTo("bob");
    }

    @Test
    public void search_whenSortedByUsernameAscending_thenReturnInThatOrder() {

        // given
        insert(userWith("charlie", "1003", Role.PLAYER), User.class);
        insert(userWith("alice", "1001", Role.PLAYER), User.class);
        insert(userWith("bob", "1002", Role.PLAYER), User.class);

        var query = new SearchUsers(null, null, null, null, null, null, UserSortField.USERNAME,
                SortDirection.ASC, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.data()).extracting("username")
                .containsExactly("alice", "bob", "charlie");
    }

    @Test
    public void search_whenPaginated_thenReportTotalsAcrossEveryPage() {

        // given
        insert(userWith("alice", "1001", Role.PLAYER), User.class);
        insert(userWith("bob", "1002", Role.PLAYER), User.class);
        insert(userWith("charlie", "1003", Role.PLAYER), User.class);

        var query = new SearchUsers(null, null, null, null, null, null, UserSortField.USERNAME,
                SortDirection.ASC, 2, 2);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.data()).hasSize(1);
    }

    @Test
    public void search_whenFilteredByPartialDisplayNameInAnotherCase_thenReturnMatchingOnly() {

        // given
        insert(userWith("alice", "Alice Cooper", "1001", Role.PLAYER), User.class);
        insert(userWith("bob", "Bob Marley", "1002", Role.PLAYER), User.class);

        var query = new SearchUsers(null, "COOP", null, null, null, null, null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(1);
        assertThat(result.data()).singleElement()
                .extracting("displayName").isEqualTo("Alice Cooper");
    }

    @Test
    public void search_whenSortedByDisplayNameAscending_thenReturnInThatOrder() {

        // given
        insert(userWith("alice", "Zelda", "1001", Role.PLAYER), User.class);
        insert(userWith("bob", "Mallory", "1002", Role.PLAYER), User.class);
        insert(userWith("charlie", "Arwen", "1003", Role.PLAYER), User.class);

        var query = new SearchUsers(null, null, null, null, null, null, UserSortField.DISPLAY_NAME,
                SortDirection.ASC, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.data()).extracting("displayName")
                .containsExactly("Arwen", "Mallory", "Zelda");
    }

    @Test
    public void search_whenUsersExist_thenEachSummaryCarriesTheDisplayName() {

        // given
        insert(userWith("alice", "Alice Cooper", "1001", Role.PLAYER), User.class);

        var query = new SearchUsers(null, null, null, null, null, null, null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.data()).singleElement()
                .extracting("username", "displayName")
                .containsExactly("alice", "Alice Cooper");
    }

    private User userWith(String username, String discordId, Role role) {

        return userWith(username, username, discordId, role);
    }

    private User userWith(String username, String displayName, String discordId, Role role) {

        return User.builder()
                .discordId(discordId)
                .username(username)
                .displayName(displayName)
                .role(role)
                .build();
    }

    private User deactivated(User user) {

        user.updateActiveState(false, "someone.else");

        return user;
    }

    private void backdateCreationDate(User user, Instant creationDate) {

        user.setCreationDate(creationDate);
        update(user, user.getId(), User.class);
    }
}
