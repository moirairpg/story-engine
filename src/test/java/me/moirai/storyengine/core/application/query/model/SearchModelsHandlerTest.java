package me.moirai.storyengine.core.application.query.model;

import static me.moirai.storyengine.common.enums.ArtificialIntelligenceModel.GPT35_TURBO;
import static me.moirai.storyengine.common.enums.ArtificialIntelligenceModel.GPT4_MINI;
import static me.moirai.storyengine.common.enums.ArtificialIntelligenceModel.GPT4_OMNI;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.core.port.inbound.model.AiModelResult;
import me.moirai.storyengine.core.port.inbound.model.SearchModels;

@ExtendWith(MockitoExtension.class)
public class SearchModelsHandlerTest {

    @InjectMocks
    private SearchModelsHandler handler;

    @Test
    public void whenNoParameters_thenAllModelsAreReturned() {

        // Given
        SearchModels query = new SearchModels(null, null);
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
        SearchModels query = new SearchModels("GPT-4 Omni", null);
        ArtificialIntelligenceModel expectedModel = GPT4_OMNI;

        // When
        List<AiModelResult> result = handler.handle(query);

        // Then
        assertThat(result).isNotNull()
                .isNotEmpty()
                .hasSize(1);

        AiModelResult modelFound = result.get(0);
        assertThat(modelFound.fullModelName()).isEqualTo(expectedModel.getFullModelName());
        assertThat(modelFound.internalModelName()).isEqualTo(expectedModel.toString());
        assertThat(modelFound.officialModelName()).isEqualTo(expectedModel.getOfficialModelName());
        assertThat(modelFound.hardTokenLimit()).isEqualTo(expectedModel.getHardTokenLimit());
    }

    @Test
    public void whenSpecificModelSearchedThroughTokenLimit_thenItIsReturned() {

        // Then
        SearchModels query = new SearchModels(null, "16385");
        ArtificialIntelligenceModel expectedModel = GPT35_TURBO;

        // When
        List<AiModelResult> result = handler.handle(query);

        // Then
        assertThat(result).isNotNull()
                .isNotEmpty()
                .hasSize(1);

        AiModelResult modelFound = result.get(0);
        assertThat(modelFound.fullModelName()).isEqualTo(expectedModel.getFullModelName());
        assertThat(modelFound.internalModelName()).isEqualTo(expectedModel.toString());
        assertThat(modelFound.officialModelName()).isEqualTo(expectedModel.getOfficialModelName());
        assertThat(modelFound.hardTokenLimit()).isEqualTo(expectedModel.getHardTokenLimit());
    }

    @Test
    public void whenGeneralModelSearchedThroughFullName_thenMatchingResultsReturned() {

        // Then
        SearchModels query = new SearchModels("gpt-4", null);
        ArtificialIntelligenceModel omni = GPT4_OMNI;
        ArtificialIntelligenceModel mini = GPT4_MINI;

        // When
        List<AiModelResult> result = handler.handle(query);

        // Then
        assertThat(result).isNotNull()
                .isNotEmpty()
                .hasSize(2);

        AiModelResult miniFound = result.get(0);
        assertThat(miniFound.fullModelName()).isEqualTo(mini.getFullModelName());
        assertThat(miniFound.internalModelName()).isEqualTo(mini.toString());
        assertThat(miniFound.officialModelName()).isEqualTo(mini.getOfficialModelName());
        assertThat(miniFound.hardTokenLimit()).isEqualTo(mini.getHardTokenLimit());

        AiModelResult omniFound = result.get(1);
        assertThat(omniFound.fullModelName()).isEqualTo(omni.getFullModelName());
        assertThat(omniFound.internalModelName()).isEqualTo(omni.toString());
        assertThat(omniFound.officialModelName()).isEqualTo(omni.getOfficialModelName());
        assertThat(omniFound.hardTokenLimit()).isEqualTo(omni.getHardTokenLimit());
    }

    @Test
    public void whenSpecificModelSearchedThroughTokenLimitDoesntExist_thenNothingIsReturned() {

        // Then
        SearchModels query = new SearchModels(null, "123456");

        // When
        List<AiModelResult> result = handler.handle(query);

        // Then
        assertThat(result).isNotNull()
                .isEmpty();
    }

    @Test
    public void whenSpecificModelSearchedThroughNameDoesntExist_thenNothingIsReturned() {

        // Then
        SearchModels query = new SearchModels("invalid_name", null);

        // When
        List<AiModelResult> result = handler.handle(query);

        // Then
        assertThat(result).isNotNull()
                .isEmpty();
    }
}
