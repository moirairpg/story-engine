package me.moirai.storyengine.core.application.query.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.userdetails.GetSignUpDetails;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;

@ExtendWith(MockitoExtension.class)
public class GetSignUpDetailsHandlerTest {

    @Mock
    private DiscordAuthenticationPort discordAuthenticationPort;

    @InjectMocks
    private GetSignUpDetailsHandler handler;

    @Test
    public void shouldReturnTheDiscordUsernameWhenTheSessionTokenResolves() {

        // given
        var discordUser = new DiscordUserDataResponse(
                "99999",
                "merlin_discord",
                null,
                null,
                null,
                null,
                null);

        when(discordAuthenticationPort.getLoggedUser("discord-token")).thenReturn(discordUser);

        // when
        var result = handler.handle(new GetSignUpDetails("discord-token"));

        // then
        assertThat(result.discordUsername()).isEqualTo("merlin_discord");
    }
}
