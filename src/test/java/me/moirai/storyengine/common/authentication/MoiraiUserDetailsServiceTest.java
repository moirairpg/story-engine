package me.moirai.storyengine.common.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authentication.MoiraiUserDetailsService;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.GetUserDetailsByDiscordId;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.UserDetailsResult;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;

@ExtendWith(MockitoExtension.class)
public class MoiraiUserDetailsServiceTest {

    @Mock
    private DiscordAuthenticationPort discordAuthenticationPort;

    @Mock
    private QueryRunner queryRunner;

    @InjectMocks
    private MoiraiUserDetailsService service;

    @Test
    public void authenticateUser_whenUserExists_thenReturnPrincipal() {

        // Given
        var token = "AUTH_TOKEN / REFRESH_TOKEN";
        var username = "john.doe";
        var nickname = "JohnDoe";

        var response = new DiscordUserDataResponse(
                null,
                username,
                nickname,
                null,
                "email@email.com",
                null);

        when(queryRunner.run(any(GetUserDetailsByDiscordId.class))).thenReturn(new UserDetailsResult(
                UUID.randomUUID(),
                "12345",
                username,
                nickname,
                "http://someurl.com/somepic.jpg",
                null,
                Instant.parse("2024-12-01T14:00:00Z")));

        when(discordAuthenticationPort.retrieveLoggedUser(anyString())).thenReturn(response);

        // When
        var userDetails = service.loadUserByUsername(token);

        // Then
        var principal = (MoiraiPrincipal) userDetails;
        assertThat(principal).isNotNull();
        assertThat(principal.getUsername()).isEqualTo(response.username());
        assertThat(principal.email()).isEqualTo(response.email());
        assertThat(principal.authorizationToken()).isEqualTo("AUTH_TOKEN");
        assertThat(principal.refreshToken()).isEqualTo("REFRESH_TOKEN");
    }
}
