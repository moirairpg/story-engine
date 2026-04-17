package me.moirai.storyengine.core.application.query.model;

import static me.moirai.storyengine.common.enums.ArtificialIntelligenceModel.GPT54;
import static me.moirai.storyengine.common.enums.ArtificialIntelligenceModel.GPT54_MINI;
import static me.moirai.storyengine.common.enums.ArtificialIntelligenceModel.GPT54_NANO;
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
        SearchModels query = new SearchModels("GPT-5.4 Mini", null);
        ArtificialIntelligenceModel expectedModel = GPT54_MINI;

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
        SearchModels query = new SearchModels(null, "1050000");
        ArtificialIntelligenceModel expectedModel = GPT54;

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
        SearchModels query = new SearchModels("5.4 ", null);
        ArtificialIntelligenceModel mini = GPT54_MINI;
        ArtificialIntelligenceModel nano = GPT54_NANO;

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

        AiModelResult nanoFound = result.get(1);
        assertThat(nanoFound.fullModelName()).isEqualTo(nano.getFullModelName());
        assertThat(nanoFound.internalModelName()).isEqualTo(nano.toString());
        assertThat(nanoFound.officialModelName()).isEqualTo(nano.getOfficialModelName());
        assertThat(nanoFound.hardTokenLimit()).isEqualTo(nano.getHardTokenLimit());
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
