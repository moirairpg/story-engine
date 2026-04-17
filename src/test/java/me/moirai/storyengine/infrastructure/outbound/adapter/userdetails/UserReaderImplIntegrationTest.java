package me.moirai.storyengine.infrastructure.outbound.adapter.userdetails;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.userdetails.UserData;
import me.moirai.storyengine.core.port.outbound.userdetails.UserReader;

public class UserReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserReader reader;

    @BeforeEach
    public void before() {
        clear(User.class);
    }

    @Test
    public void getUserByDiscordId_whenNotFound_thenReturnEmpty() {

        // Given
        var discordId = "nonexistent";

        // When
        var result = reader.getUserByDiscordId(discordId);

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    public void getUserByDiscordId_whenFound_thenReturnUserData() {

        // Given
        var user = insert(UserFixture.player().build(), User.class);

        // When
        Optional<UserData> result = reader.getUserByDiscordId(user.getDiscordId());

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result.get().discordId()).isEqualTo(user.getDiscordId());
        assertThat(result.get().publicId()).isEqualTo(user.getPublicId());
        assertThat(result.get().role()).isEqualTo(user.getRole());
    }
}
