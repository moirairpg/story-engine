package me.moirai.storyengine.core.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.GenerateImage;
import me.moirai.storyengine.core.port.outbound.ai.ImageGenerationPort;

@ExtendWith(MockitoExtension.class)
public class GenerateImageHandlerTest {

    @Mock
    private ImageGenerationPort imageGenerationPort;

    @InjectMocks
    private GenerateImageHandler handler;

    @Test
    public void shouldReturnImageBytesWhenPromptIsValid() {

        // given
        var prompt = "Generate a fantasy world";
        var expected = new byte[]{1, 2, 3};
        var command = new GenerateImage(prompt);

        when(imageGenerationPort.generate(prompt)).thenReturn(expected);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void shouldDelegatePromptToPort() {

        // given
        var prompt = "Generate a dark fantasy dungeon";
        var command = new GenerateImage(prompt);

        when(imageGenerationPort.generate(prompt)).thenReturn(new byte[]{4, 5, 6});

        // when
        handler.handle(command);

        // then
        verify(imageGenerationPort).generate(prompt);
    }
}
