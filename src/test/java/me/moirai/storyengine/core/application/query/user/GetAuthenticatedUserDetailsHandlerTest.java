package me.moirai.storyengine.core.application.query.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.userdetails.GetAuthenticatedUserDetails;
import me.moirai.storyengine.core.port.inbound.userdetails.UserData;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserReader;

@ExtendWith(MockitoExtension.class)
public class GetAuthenticatedUserDetailsHandlerTest {

    private static final UUID PUBLIC_ID = UUID.fromString("aabbccdd-1111-0000-0000-000000000000");
    private static final Long NUMERIC_ID = 1L;
    private static final String DISCORD_ID = "123456789";
    private static final String DISCORD_TOKEN = "test-discord-token";

    @Mock
    private UserReader userReader;

    @Mock
    private DiscordAuthenticationPort discordAuthenticationPort;

    @InjectMocks
    private GetAuthenticatedUserDetailsHandler handler;

    @Test
    public void shouldReturnUserDetailsWhenUserIsFound() {

        // given
        var query = new GetAuthenticatedUserDetails(DISCORD_TOKEN);
        var discordResponse = new DiscordUserDataResponse(DISCORD_ID, "john.doe", "John Doe", "avatar123", null, "john@example.com", null, null);
        var userData = new UserData(PUBLIC_ID, NUMERIC_ID, DISCORD_ID, Role.PLAYER, Instant.now());

        when(discordAuthenticationPort.getLoggedUser(DISCORD_TOKEN)).thenReturn(discordResponse);
        when(userReader.getUserByDiscordId(DISCORD_ID)).thenReturn(Optional.of(userData));

        // when
        var result = handler.handle(query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.publicId()).isEqualTo(PUBLIC_ID);
        assertThat(result.id()).isEqualTo(NUMERIC_ID);
        assertThat(result.discordId()).isEqualTo(DISCORD_ID);
        assertThat(result.username()).isEqualTo("john.doe");
        assertThat(result.nickname()).isEqualTo("John Doe");
        assertThat(result.role()).isEqualTo(Role.PLAYER);
    }

    @Test
    public void shouldFallbackToUsernameWhenGlobalNicknameIsNull() {

        // given
        var query = new GetAuthenticatedUserDetails(DISCORD_TOKEN);
        var discordResponse = new DiscordUserDataResponse(DISCORD_ID, "john.doe", null, "avatar123", null, "john@example.com", null, null);
        var userData = new UserData(PUBLIC_ID, NUMERIC_ID, DISCORD_ID, Role.PLAYER, Instant.now());

        when(discordAuthenticationPort.getLoggedUser(DISCORD_TOKEN)).thenReturn(discordResponse);
        when(userReader.getUserByDiscordId(DISCORD_ID)).thenReturn(Optional.of(userData));

        // when
        var result = handler.handle(query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.nickname()).isEqualTo("john.doe");
    }

    @Test
    public void shouldThrowWhenUserIsNotRegisteredInMoirai() {

        // given
        var query = new GetAuthenticatedUserDetails(DISCORD_TOKEN);
        var discordResponse = new DiscordUserDataResponse(DISCORD_ID, "john.doe", "John Doe", "avatar123", null, "john@example.com", null, null);

        when(discordAuthenticationPort.getLoggedUser(DISCORD_TOKEN)).thenReturn(discordResponse);
        when(userReader.getUserByDiscordId(DISCORD_ID)).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.handle(query));
    }
}
