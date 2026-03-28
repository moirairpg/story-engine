package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.moirai.storyengine.AbstractWebMockTest;

class TextModerationAdapterTest extends AbstractWebMockTest {

    private TextModerationAdapter adapter;

    @BeforeEach
    void before() {

        var restClient = RestClient.builder()
                .baseUrl("http://localhost:" + PORT)
                .build();

        adapter = new TextModerationAdapter("/moderation", restClient);
    }

    @Test
    void shouldReturnModerationResultWhenValidRequest() throws JsonProcessingException {

        var expectedResponse = ModerationResponse.builder()
                .model("gpt-3.5")
                .id("id123")
                .results(Collections.singletonList(ModerationResult.builder()
                        .flagged(false)
                        .categoryScores(Collections.singletonMap("topic", "0.7"))
                        .categories(Collections.singletonMap("topic", true))
                        .build()))
                .build();

        prepareWebserverFor(expectedResponse, 200);

        var result = adapter.moderate("This is the input");

        assertThat(result).isNotNull();
        assertThat(result.moderationScores())
                .containsAllEntriesOf(Collections.singletonMap("topic", 0.7));
    }
}
