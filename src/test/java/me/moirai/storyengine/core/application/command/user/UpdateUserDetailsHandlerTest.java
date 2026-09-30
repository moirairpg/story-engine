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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUserDetails;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateUserDetailsHandlerTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UpdateUserDetailsHandler handler;

    @Test
    public void shouldUpdateAndSaveTheDisplayNameAndBioWhenTheUserExists() {

        // given
        var user = UserFixture.playerWithId();
        var command = new UpdateUserDetails("john.doe", "Merlin the Grey", "A wandering bard.");

        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        // when
        handler.handle(command);

        // then
        verify(repository).save(user);

        assertThat(user.getDisplayName()).isEqualTo("Merlin the Grey");
        assertThat(user.getBio()).isEqualTo("A wandering bard.");
    }

    @Test
    public void shouldThrowExceptionWhenTheHandleIsUnknown() {

        // given
        var command = new UpdateUserDetails("ghost", "Merlin the Grey", "A wandering bard.");

        when(repository.findByUsername("ghost")).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void shouldThrowExceptionWhenTheDisplayNameIsInvalid() {

        // given
        var command = new UpdateUserDetails("john.doe", "M", "A wandering bard.");

        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(UserFixture.playerWithId()));

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(repository, never()).save(any(User.class));
    }
}
