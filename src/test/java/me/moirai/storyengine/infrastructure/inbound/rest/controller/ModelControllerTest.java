package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;

import me.moirai.storyengine.AbstractRestWebTest;
import me.moirai.storyengine.core.port.inbound.model.SearchModels;
import me.moirai.storyengine.core.port.inbound.model.AiModelResult;
import me.moirai.storyengine.core.application.usecase.model.result.AiModelResultFixture;
import me.moirai.storyengine.infrastructure.inbound.rest.response.AiModelResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.AiModelResponseFixture;
import me.moirai.storyengine.infrastructure.security.authentication.config.AuthenticationSecurityConfig;

@WebFluxTest(controllers = {
        ModelController.class
}, excludeAutoConfiguration = {
        ReactiveSecurityAutoConfiguration.class,
        AuthenticationSecurityConfig.class
})
public class ModelControllerTest extends AbstractRestWebTest {

    private static final String MODEL_BASE_URL = "/model";

    @Test
    public void http200WhenSearchModels() {

        // Given
        List<AiModelResult> result = list(AiModelResultFixture.gpt35turbo().build());
        AiModelResponse expectedResponse = AiModelResponseFixture.gpt35turbo().build();

        when(useCaseRunner.run(any(SearchModels.class))).thenReturn(result);

        // Then
        webTestClient.get()
                .uri(MODEL_BASE_URL)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(AiModelResponse.class)
                .value(response -> {
                    AiModelResponse model = (AiModelResponse) response.get(0);
                    assertThat(model.getFullModelName()).isEqualTo(expectedResponse.getFullModelName());
                    assertThat(model.getInternalModelName()).isEqualTo(expectedResponse.getInternalModelName());
                    assertThat(model.getOfficialModelName()).isEqualTo(expectedResponse.getOfficialModelName());
                    assertThat(model.getHardTokenLimit()).isEqualTo(expectedResponse.getHardTokenLimit());
                });
    }

    @Test
    public void http200WhenSearchModelsWithSpecificName() {

        // Given
        List<AiModelResult> result = list(AiModelResultFixture.gpt35turbo().build());
        AiModelResponse expectedResponse = AiModelResponseFixture.gpt35turbo().build();

        when(useCaseRunner.run(any(SearchModels.class))).thenReturn(result);

        // Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(MODEL_BASE_URL)
                        .queryParam("modelName", "gpt-3")
                        .build())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(AiModelResponse.class)
                .value(response -> {
                    AiModelResponse model = (AiModelResponse) response.get(0);
                    assertThat(model.getFullModelName()).isEqualTo(expectedResponse.getFullModelName());
                    assertThat(model.getInternalModelName()).isEqualTo(expectedResponse.getInternalModelName());
                    assertThat(model.getOfficialModelName()).isEqualTo(expectedResponse.getOfficialModelName());
                    assertThat(model.getHardTokenLimit()).isEqualTo(expectedResponse.getHardTokenLimit());
                });
    }

    @Test
    public void http200WhenSearchModelsWithSpecificTokenLimit() {

        // Given
        List<AiModelResult> result = list(AiModelResultFixture.gpt35turbo().build());
        AiModelResponse expectedResponse = AiModelResponseFixture.gpt35turbo().build();

        when(useCaseRunner.run(any(SearchModels.class))).thenReturn(result);

        // Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(MODEL_BASE_URL)
                        .queryParam("tokenLimit", "123")
                        .build())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(AiModelResponse.class)
                .value(response -> {
                    AiModelResponse model = (AiModelResponse) response.get(0);
                    assertThat(model.getFullModelName()).isEqualTo(expectedResponse.getFullModelName());
                    assertThat(model.getInternalModelName()).isEqualTo(expectedResponse.getInternalModelName());
                    assertThat(model.getOfficialModelName()).isEqualTo(expectedResponse.getOfficialModelName());
                    assertThat(model.getHardTokenLimit()).isEqualTo(expectedResponse.getHardTokenLimit());
                });
    }
}
