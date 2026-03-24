package me.moirai.storyengine.core.application.command.adventure;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureAuthorsNoteByChannelId;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateAdventureAuthorsNoteByChannelIdHandlerTest {

    @Mock
    private AdventureRepository repository;

    @InjectMocks
    private UpdateAdventureAuthorsNoteByChannelIdHandler handler;

    @Test
    public void updateAuthorsNote_whenAdventureNotFound_thenThrowException() {

        // given
        var requesterId = "123123";
        var command = UpdateAdventureAuthorsNoteByChannelId.builder()
                .authorsNote("AuthorsNote")
                .requesterId(requesterId)
                .channelId("1234123")
                .build();

        when(repository.findByChannelId(anyString())).thenReturn(Optional.empty());

        // then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void updateAuthorsNote_whenCalled_thenUpdateAdventureAuthorsNote() {

        // given
        var requesterId = "4245345";
        var command = UpdateAdventureAuthorsNoteByChannelId.builder()
                .authorsNote("AuthorsNote")
                .channelId("1234123")
                .requesterId(requesterId)
                .build();

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();

        when(repository.findByChannelId(anyString())).thenReturn(Optional.of(adventure));

        // when
        handler.execute(command);

        // then
        verify(repository, times(1))
                .updateAuthorsNoteByChannelId(anyString(), anyString());
    }
}
