package me.moirai.storyengine.infrastructure.security.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.GetUserDetailsByDiscordId;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.UserDetailsResult;
import me.moirai.storyengine.core.port.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.DiscordUserDataResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class MoiraiUserDetailsServiceTest {

    @Mock
    private DiscordAuthenticationPort discordAuthenticationPort;

    @Mock
    private UseCaseRunner useCaseRunner;

    @InjectMocks
    private MoiraiUserDetailsService service;

    @Test
    public void authenticateUser_whenUserExists_thenReturnPrincipal() {

        // Given
        String token = "AUTH_TOKEN / REFRESH_TOKEN";
        String username = "john.doe";
        String nickname = "JohnDoe";

        DiscordUserDataResponse response = DiscordUserDataResponse.builder()
                .globalNickname(nickname)
                .username(username)
                .email("email@email.com")
                .build();

        when(useCaseRunner.run(any(GetUserDetailsByDiscordId.class))).thenReturn(UserDetailsResult.builder()
                .avatarUrl("http://someurl.com/somepic.jpg")
                .discordId("12345")
                .nickname(nickname)
                .username(username)
                .joinDate(OffsetDateTime.parse("2024-12-01T14:00:00Z"))
                .build());

        when(discordAuthenticationPort.retrieveLoggedUser(anyString())).thenReturn(Mono.just(response));

        // Then
        StepVerifier.create(service.findByUsername(token))
                .assertNext(userDetails -> {
                    MoiraiPrincipal principal = (MoiraiPrincipal) userDetails;
                    assertThat(principal).isNotNull();
                    assertThat(principal.getUsername()).isEqualTo(response.getUsername());
                    assertThat(principal.getEmail()).isEqualTo(response.getEmail());
                    assertThat(principal.getAuthorizationToken()).isEqualTo("AUTH_TOKEN");
                    assertThat(principal.getRefreshToken()).isEqualTo("REFRESH_TOKEN");
                })
                .verifyComplete();
    }
}
