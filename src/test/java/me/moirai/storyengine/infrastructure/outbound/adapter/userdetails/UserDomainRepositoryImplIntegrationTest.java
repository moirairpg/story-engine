package me.moirai.storyengine.infrastructure.outbound.adapter.userdetails;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractDatabaseIntegrationTest;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

public class UserDomainRepositoryImplIntegrationTest extends AbstractDatabaseIntegrationTest {

    @Autowired
    private UserRepository repository;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void shouldReturnTheUserAsStoredWhenFindingByHandleInAnotherCase() {

        // given
        insertUser("11111", "Merlin");

        // when
        var result = repository.findByUsername("mERLIN");

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get().getUsername()).isEqualTo("Merlin");
    }

    @Test
    public void shouldReturnEmptyWhenFindingByAnUnknownHandle() {

        // given
        insertUser("11111", "Merlin");

        // when
        var result = repository.findByUsername("morgana");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnEveryExistingMatchAndNothingForTheUnknownWhenFindingAllByHandlesInMixedCase() {

        // given
        insertUser("11111", "Merlin");
        insertUser("22222", "arthur");
        insertUser("33333", "gawain");

        // when
        var result = repository.findAllByUsernameIn(List.of("MERLIN", "Arthur", "ghost"));

        // then
        assertThat(result)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("Merlin", "arthur");
    }

    private User insertUser(String discordId, String username) {

        return insert(UserFixture.player()
                .discordId(discordId)
                .username(username)
                .build(), User.class);
    }
}
