package me.moirai.storyengine.core.application.command.user;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUserRole;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateUserRoleHandlerTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UpdateUserRoleHandler handler;

    @Test
    public void shouldUpdateRoleWhenUserIsFound() {

        // given
        var command = new UpdateUserRole(UserFixture.PUBLIC_ID, Role.ADMIN);
        var user = UserFixture.playerWithId();

        when(repository.findByPublicId(UserFixture.PUBLIC_ID)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        // when
        handler.handle(command);

        // then
        verify(repository).save(user);
    }

    @Test
    public void shouldThrowWhenUserIsNotFound() {

        // given
        var command = new UpdateUserRole(UserFixture.PUBLIC_ID, Role.ADMIN);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowWhenUserIdIsNull() {

        // given
        var command = new UpdateUserRole(null, Role.ADMIN);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowWhenRoleIsNull() {

        // given
        var command = new UpdateUserRole(UserFixture.PUBLIC_ID, null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }
}
