package me.moirai.storyengine.core.application.usecase.model;

import static me.moirai.storyengine.core.domain.adventure.ArtificialIntelligenceModel.GPT35_TURBO;
import static me.moirai.storyengine.core.domain.adventure.ArtificialIntelligenceModel.GPT4_MINI;
import static me.moirai.storyengine.core.domain.adventure.ArtificialIntelligenceModel.GPT4_OMNI;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.model.SearchModels;
import me.moirai.storyengine.core.port.inbound.model.AiModelResult;
import me.moirai.storyengine.core.domain.adventure.ArtificialIntelligenceModel;

@ExtendWith(MockitoExtension.class)
public class SearchModelsHandlerTest {

    @InjectMocks
    private SearchModelsHandler handler;

    @Test
    public void whenNoParameters_thenAllModelsAreReturned() {

        // Given
        SearchModels query = SearchModels.build(null, null);
        int expectedModelAmount = ArtificialIntelligenceModel.values().length;

        // When
        List<AiModelResult> result = handler.handle(query);

        // Then
        assertThat(result).isNotNull()
                .isNotEmpty()
                .hasSize(expectedModelAmount);
    }

    @Test
    public void whenSpecificModelSearchedThroughFullName_thenItIsReturned() {

        // Then
        SearchModels query = SearchModels.build("GPT-4 Omni", null);
        ArtificialIntelligenceModel expectedModel = GPT4_OMNI;

        // When
        List<AiModelResult> result = handler.handle(query);

        // Then
        assertThat(result).isNotNull()
                .isNotEmpty()
                .hasSize(1);

        AiModelResult modelFound = result.get(0);
        assertThat(modelFound.getFullModelName()).isEqualTo(expectedModel.getFullModelName());
        assertThat(modelFound.getInternalModelName()).isEqualTo(expectedModel.toString());
        assertThat(modelFound.getOfficialModelName()).isEqualTo(expectedModel.getOfficialModelName());
        assertThat(modelFound.getHardTokenLimit()).isEqualTo(expectedModel.getHardTokenLimit());
    }

    @Test
    public void whenSpecificModelSearchedThroughTokenLimit_thenItIsReturned() {

        // Then
        SearchModels query = SearchModels.build(null, "16385");
        ArtificialIntelligenceModel expectedModel = GPT35_TURBO;

        // When
        List<AiModelResult> result = handler.handle(query);

        // Then
        assertThat(result).isNotNull()
                .isNotEmpty()
                .hasSize(1);

        AiModelResult modelFound = result.get(0);
        assertThat(modelFound.getFullModelName()).isEqualTo(expectedModel.getFullModelName());
        assertThat(modelFound.getInternalModelName()).isEqualTo(expectedModel.toString());
        assertThat(modelFound.getOfficialModelName()).isEqualTo(expectedModel.getOfficialModelName());
        assertThat(modelFound.getHardTokenLimit()).isEqualTo(expectedModel.getHardTokenLimit());
    }

    @Test
    public void whenGeneralModelSearchedThroughFullName_thenMatchingResultsReturned() {

        // Then
        SearchModels query = SearchModels.build("gpt-4", null);
        ArtificialIntelligenceModel omni = GPT4_OMNI;
        ArtificialIntelligenceModel mini = GPT4_MINI;

        // When
        List<AiModelResult> result = handler.handle(query);

        // Then
        assertThat(result).isNotNull()
                .isNotEmpty()
                .hasSize(2);

        AiModelResult miniFound = result.get(0);
        assertThat(miniFound.getFullModelName()).isEqualTo(mini.getFullModelName());
        assertThat(miniFound.getInternalModelName()).isEqualTo(mini.toString());
        assertThat(miniFound.getOfficialModelName()).isEqualTo(mini.getOfficialModelName());
        assertThat(miniFound.getHardTokenLimit()).isEqualTo(mini.getHardTokenLimit());

        AiModelResult omniFound = result.get(1);
        assertThat(omniFound.getFullModelName()).isEqualTo(omni.getFullModelName());
        assertThat(omniFound.getInternalModelName()).isEqualTo(omni.toString());
        assertThat(omniFound.getOfficialModelName()).isEqualTo(omni.getOfficialModelName());
        assertThat(omniFound.getHardTokenLimit()).isEqualTo(omni.getHardTokenLimit());
    }

    @Test
    public void whenSpecificModelSearchedThroughTokenLimitDoesntExist_thenNothingIsReturned() {

        // Then
        SearchModels query = SearchModels.build(null, "123456");

        // When
        List<AiModelResult> result = handler.handle(query);

        // Then
        assertThat(result).isNotNull()
                .isEmpty();
    }

    @Test
    public void whenSpecificModelSearchedThroughNameDoesntExist_thenNothingIsReturned() {

        // Then
        SearchModels query = SearchModels.build("invalid_name", null);

        // When
        List<AiModelResult> result = handler.handle(query);

        // Then
        assertThat(result).isNotNull()
                .isEmpty();
    }
}
