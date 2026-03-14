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
import me.moirai.storyengine.core.port.inbound.discord.DiscordUserDetails;
import me.moirai.storyengine.core.application.usecase.discord.DiscordUserDetailsFixture;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.GetUserDetailsByDiscordId;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.UserDetailsResult;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserDomainRepository;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.DiscordUserDetailsPort;

public class GetUserDetailsByDiscordIdHandlerTest extends AbstractDiscordTest {

    @Mock
    private UserDomainRepository repository;

    @Mock
    private DiscordUserDetailsPort discordUserDetailsPort;

    @InjectMocks
    private GetUserDetailsByDiscordIdHandler handler;

    @Test
    public void retrieveUser_whenUserIsFound_thenReturnUserData() {

        // Given
        GetUserDetailsByDiscordId query = GetUserDetailsByDiscordId.build("1234");
        DiscordUserDetails userDetails = DiscordUserDetailsFixture.create()
                .id(query.getDiscordUserId())
                .build();

        User user = UserFixture.player()
                .discordId(query.getDiscordUserId())
                .build();

        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.of(userDetails));
        when(repository.findByDiscordId(anyString())).thenReturn(Optional.of(user));

        // When
        UserDetailsResult result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiscordId()).isEqualTo(query.getDiscordUserId());
        assertThat(result.getNickname()).isEqualTo("natalis");
        assertThat(result.getUsername()).isEqualTo("john.natalis");
    }

    @Test
    public void retrieveUser_whenUserIsFound_andNicknameIsNull_thenReturnUserDataWithUsernameAsNickname() {

        // Given
        GetUserDetailsByDiscordId query = GetUserDetailsByDiscordId.build("1234");
        DiscordUserDetails userDetails = DiscordUserDetailsFixture.create()
                .id(query.getDiscordUserId())
                .nickname(null)
                .build();

        User user = UserFixture.player()
                .discordId(query.getDiscordUserId())
                .build();

        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.of(userDetails));
        when(repository.findByDiscordId(anyString())).thenReturn(Optional.of(user));

        // When
        UserDetailsResult result = handler.execute(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiscordId()).isEqualTo(query.getDiscordUserId());
        assertThat(result.getNickname()).isEqualTo("john.natalis");
        assertThat(result.getUsername()).isEqualTo("john.natalis");
    }

    @Test
    public void retrieveUser_whenUserNotExistsInDiscord_thenThrowException() {

        // Given
        String expectedMessage = "The Discord User with the requested ID does not exist";
        GetUserDetailsByDiscordId query = GetUserDetailsByDiscordId.build("1234");

        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(DiscordApiException.class)
                .isThrownBy(() -> handler.execute(query))
                .withMessage(expectedMessage);
    }

    @Test
    public void retrieveUser_whenUserNotRegistered_thenThrowException() {

        // Given
        String expectedMessage = "The User with the requested ID is not registered in MoirAI";
        GetUserDetailsByDiscordId query = GetUserDetailsByDiscordId.build("1234");

        DiscordUserDetails userDetails = DiscordUserDetailsFixture.create()
                .id(query.getDiscordUserId())
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
        String expectedMessage = "Discord ID cannot be null";
        String userId = null;
        GetUserDetailsByDiscordId query = GetUserDetailsByDiscordId.build(userId);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(query))
                .withMessage(expectedMessage);
    }
}
