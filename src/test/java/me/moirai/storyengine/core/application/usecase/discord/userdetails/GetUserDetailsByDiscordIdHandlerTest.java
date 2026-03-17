package me.moirai.storyengine.core.application.usecase.discord.userdetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import me.moirai.storyengine.AbstractDiscordTest;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.DiscordApiException;
import me.moirai.storyengine.core.application.usecase.discord.DiscordUserDetailsFixture;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.GetUserDetailsByDiscordId;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDetailsPort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

public class GetUserDetailsByDiscordIdHandlerTest extends AbstractDiscordTest {

    @Mock
    private UserRepository repository;

    @Mock
    private DiscordUserDetailsPort discordUserDetailsPort;

    @InjectMocks
    private GetUserDetailsByDiscordIdHandler handler;

    @Test
    public void retrieveUser_whenUserIsFound_thenReturnUserData() {

        // Given
        var query = new GetUserDetailsByDiscordId("1234");
        var userDetails = DiscordUserDetailsFixture.create()
                .id(query.discordUserId())
                .build();

        var user = UserFixture.player()
                .discordId(query.discordUserId())
                .build();

        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.of(userDetails));
        when(repository.findByDiscordId(anyString())).thenReturn(Optional.of(user));

        // When
        var result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.discordId()).isEqualTo(query.discordUserId());
        assertThat(result.nickname()).isEqualTo("natalis");
        assertThat(result.username()).isEqualTo("john.natalis");
    }

    @Test
    public void retrieveUser_whenUserIsFound_andNicknameIsNull_thenReturnUserDataWithUsernameAsNickname() {

        // Given
        var query = new GetUserDetailsByDiscordId("1234");
        var userDetails = DiscordUserDetailsFixture.create()
                .id(query.discordUserId())
                .nickname(null)
                .build();

        var user = UserFixture.player()
                .discordId(query.discordUserId())
                .build();

        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.of(userDetails));
        when(repository.findByDiscordId(anyString())).thenReturn(Optional.of(user));

        // When
        var result = handler.execute(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.discordId()).isEqualTo(query.discordUserId());
        assertThat(result.nickname()).isEqualTo("john.natalis");
        assertThat(result.username()).isEqualTo("john.natalis");
    }

    @Test
    public void retrieveUser_whenUserNotExistsInDiscord_thenThrowException() {

        // Given
        var expectedMessage = "The Discord User with the requested ID does not exist";
        var query = new GetUserDetailsByDiscordId("1234");

        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(DiscordApiException.class)
                .isThrownBy(() -> handler.execute(query))
                .withMessage(expectedMessage);
    }

    @Test
    public void retrieveUser_whenUserNotRegistered_thenThrowException() {

        // Given
        var expectedMessage = "The User with the requested ID is not registered in MoirAI";
        var query = new GetUserDetailsByDiscordId("1234");

        var userDetails = DiscordUserDetailsFixture.create()
                .id(query.discordUserId())
                .build();

        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.of(userDetails));
        when(repository.findByDiscordId(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(AssetNotFoundException.class)
                .isThrownBy(() -> handler.execute(query))
                .withMessage(expectedMessage);
    }

    @Test
    public void retrieveUser_whenUserIdIsNull_thenThrowException() {

        // Given
        String userId = null;
        var expectedMessage = "Discord ID cannot be null";
        var query = new GetUserDetailsByDiscordId(userId);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(query))
                .withMessage(expectedMessage);
    }
}
