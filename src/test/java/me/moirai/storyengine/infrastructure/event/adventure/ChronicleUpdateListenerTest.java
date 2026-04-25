package me.moirai.storyengine.infrastructure.event.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.core.application.event.adventure.AdventureMessageWindowOverflowedEvent;
import me.moirai.storyengine.core.port.inbound.chronicle.UpdateChronicle;

@ExtendWith(MockitoExtension.class)
class ChronicleUpdateListenerTest {

    @Mock
    private CommandRunner commandRunner;

    @InjectMocks
    private ChronicleUpdateListener listener;

    @Test
    void shouldDispatchUpdateChronicleCommandWhenOverflowEventReceived() {

        // given
        var adventurePublicId = UUID.randomUUID();
        var event = new AdventureMessageWindowOverflowedEvent(adventurePublicId);

        var captor = ArgumentCaptor.forClass(UpdateChronicle.class);

        // when
        listener.onAdventureMessageSaved(event);

        // then
        verify(commandRunner).run(captor.capture());
        assertThat(captor.getValue().adventurePublicId()).isEqualTo(adventurePublicId);
    }
}
