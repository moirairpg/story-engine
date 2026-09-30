package me.moirai.storyengine.core.application.command.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.userdetails.CreateUser;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CreateUserHandlerTest {

    private static final String DISCORD_TOKEN = "discord-token";
    private static final String DISCORD_ID = "99999";

    @Mock
    private UserRepository repository;

    @Mock
    private DiscordAuthenticationPort discordAuthenticationPort;

    @InjectMocks
    private CreateUserHandler handler;

    @Test
    public void shouldSaveTheUserWhenDiscordAccountIsUnregisteredAndHandleIsFree() {

        // given
        var command = new CreateUser(DISCORD_TOKEN, "Merlin", "Merlin the Grey");
        var userCaptor = ArgumentCaptor.forClass(User.class);

        when(discordAuthenticationPort.getLoggedUser(DISCORD_TOKEN)).thenReturn(discordUser());
        when(repository.findByDiscordId(DISCORD_ID)).thenReturn(Optional.empty());
        when(repository.findByUsername("Merlin")).thenReturn(Optional.empty());

        // when
        handler.handle(command);

        // then
        verify(repository).save(userCaptor.capture());

        var saved = userCaptor.getValue();

        assertThat(saved.getDiscordId()).isEqualTo(DISCORD_ID);
        assertThat(saved.getUsername()).isEqualTo("Merlin");
        assertThat(saved.getDisplayName()).isEqualTo("Merlin the Grey");
        assertThat(saved.getRole()).isEqualTo(Role.PLAYER);
    }

    @Test
    public void shouldSaveNothingWhenDiscordAccountIsAlreadyRegistered() {

        // given
        var command = new CreateUser(DISCORD_TOKEN, "Merlin", "Merlin the Grey");

        when(discordAuthenticationPort.getLoggedUser(DISCORD_TOKEN)).thenReturn(discordUser());
        when(repository.findByDiscordId(DISCORD_ID)).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        handler.handle(command);

        // then
        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void shouldThrowExceptionWhenHandleIsAlreadyHeldInAnyCase() {

        // given
        var command = new CreateUser(DISCORD_TOKEN, "mERLIN", "Merlin the Grey");
        var holder = UserFixture.player().username("Merlin").build();

        when(discordAuthenticationPort.getLoggedUser(DISCORD_TOKEN)).thenReturn(discordUser());
        when(repository.findByDiscordId(DISCORD_ID)).thenReturn(Optional.empty());
        when(repository.findByUsername("mERLIN")).thenReturn(Optional.of(holder));

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("This username is already taken");

        verify(repository, never()).save(any(User.class));
    }

    private DiscordUserDataResponse discordUser() {

        return new DiscordUserDataResponse(
                DISCORD_ID,
                "merlin_discord",
                null,
                null,
                null,
                null,
                null);
    }
}
