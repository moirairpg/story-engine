package me.moirai.storyengine.common.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.world.CreateWorldHandler;
import me.moirai.storyengine.core.port.inbound.world.CreateWorld;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class CommandRunnerImplTest {

    @Test
    public void errorWhenHandlerNotFound() {

        // Given
        UseCaseRunnerImpl runner = new UseCaseRunnerImpl();
        CreateWorld command = CreateWorld.builder().build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> runner.run(command));
    }

    @Test
    public void errorWhenHandlerAlreadyRegistered() {

        // Given
        UseCaseRunnerImpl runner = new UseCaseRunnerImpl();
        CreateWorldHandler handler = mock(CreateWorldHandler.class);

        runner.registerHandler(handler);

        // Then
        assertThrows(IllegalArgumentException.class,
                () -> runner.registerHandler(mock(CreateWorldHandler.class)));
    }

    @Test
    public void errorWhenHandlerIsNull() {

        // Given
        UseCaseRunnerImpl runner = new UseCaseRunnerImpl();
        CreateWorldHandler handler = null;

        // Then
        assertThrows(IllegalArgumentException.class,
                () -> runner.registerHandler(handler));
    }

    @Test
    public void registerAndRun() {

        // Given
        String id = "WLRDID";
        UseCaseRunnerImpl runner = new UseCaseRunnerImpl();
        CreateWorldHandler handler = mock(CreateWorldHandler.class);
        CreateWorld command = CreateWorld.builder().build();
        WorldDetails expectedResult = WorldDetails.builder().id(id).build();

        when(handler.handle(any(CreateWorld.class))).thenReturn(Mono.just(expectedResult));

        runner.registerHandler(handler);

        // Then
        StepVerifier.create(runner.run(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getId()).isEqualTo(id);
                })
                .verifyComplete();
    }
}
