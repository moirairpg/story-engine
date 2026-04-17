package me.moirai.storyengine.core.application.command.adventure;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureNudgeById;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateAdventureNudgeByIdHandlerTest {

    @Mock
    private AdventureRepository repository;

    @InjectMocks
    private UpdateAdventureNudgeByIdHandler handler;

    @Test
    public void updateNudge_whenAdventureNotFound_thenThrowException() {

        // given
        var command = new UpdateAdventureNudgeById(
                "Nudge",
                AdventureFixture.PUBLIC_ID);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void updateNudge_whenCalled_thenUpdateAdventureNudge() {

        // given
        var command = new UpdateAdventureNudgeById(
                "Nudge",
                AdventureFixture.PUBLIC_ID);

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));

        // when
        handler.execute(command);

        // then
        verify(repository, times(1))
                .updateNudgeByPublicId(anyString(), any(UUID.class));
    }
}
