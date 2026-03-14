package me.moirai.storyengine.core.application.usecase.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.world.request.CreateWorldFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntryRepository;
import me.moirai.storyengine.core.domain.world.WorldRepository;
import me.moirai.storyengine.core.port.TextModerationPort;
import me.moirai.storyengine.core.port.inbound.world.CreateWorld;
import me.moirai.storyengine.core.port.outbound.TextModerationResult;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class CreateWorldHandlerTest {

    @Mock
    private TextModerationPort moderationPort;

    @Mock
    private WorldLorebookEntryRepository lorebookEntryRepository;

    @Mock
    private WorldRepository repository;

    @InjectMocks
    private CreateWorldHandler handler;

    @Test
    public void errorWhenCommandIsNull() {

        // Given
        CreateWorld command = null;

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createWorld() {

        // Given
        String id = "HAUDHUAHD";
        World world = WorldFixture.privateWorld().id(id).build();
        CreateWorld command = CreateWorldFixture.createPrivateWorld().build();

        TextModerationResult moderationResult = TextModerationResult.builder()
                .contentFlagged(false)
                .build();

        when(moderationPort.moderate(anyString())).thenReturn(Mono.just(moderationResult));
        when(repository.save(any(World.class))).thenReturn(world);

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getId()).isEqualTo(id);
                })
                .verifyComplete();
    }
}
