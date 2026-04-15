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

        // given
        var expectedResponse = ModerationResponse.builder()
                .model("omni-moderation-latest")
                .id("id123")
                .results(Collections.singletonList(ModerationResult.builder()
                        .flagged(false)
                        .categoryScores(Collections.singletonMap("topic", "0.7"))
                        .categories(Collections.singletonMap("topic", true))
                        .build()))
                .build();

        prepareWebserverFor(expectedResponse, 200);

        // when
        var results = adapter.moderate("This is the input");

        // then
        assertThat(results).isNotNull().hasSize(1);
        assertThat(results.get(0).moderationScores())
                .containsAllEntriesOf(Collections.singletonMap("topic", 0.7));
    }
}
