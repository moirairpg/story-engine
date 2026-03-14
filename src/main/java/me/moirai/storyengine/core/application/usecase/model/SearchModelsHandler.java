package me.moirai.storyengine.core.application.usecase.model;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Arrays;
import java.util.List;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.application.usecase.model.request.SearchModels;
import me.moirai.storyengine.core.application.usecase.model.result.AiModelResult;
import me.moirai.storyengine.core.domain.adventure.ArtificialIntelligenceModel;

@UseCaseHandler
public class SearchModelsHandler extends AbstractUseCaseHandler<SearchModels, List<AiModelResult>> {

    @Override
    public List<AiModelResult> execute(SearchModels useCase) {

        List<AiModelResult> aiModels = Arrays.asList(ArtificialIntelligenceModel.values())
                .stream()
                .map(this::toResult)
                .toList();

        return aiModels.stream()
                .filter(aiModel -> matchModelName(useCase, aiModel))
                .filter(aiModel -> matchTokenLimit(useCase, aiModel))
                .toList();
    }

    private boolean matchModelName(SearchModels useCase, AiModelResult aiModel) {

        if (isEmpty(useCase.getModelToSearch())) {
            return true;
        }

        return aiModel.getFullModelName().contains(useCase.getModelToSearch()) ||
                aiModel.getInternalModelName().contains(useCase.getModelToSearch()) ||
                aiModel.getOfficialModelName().contains(useCase.getModelToSearch());
    }

    private boolean matchTokenLimit(SearchModels useCase, AiModelResult aiModel) {

        if (isEmpty(useCase.getTokenLimit())) {
            return true;
        }

        return aiModel.getHardTokenLimit() == Long.valueOf(useCase.getTokenLimit());
    }

    private AiModelResult toResult(ArtificialIntelligenceModel model) {

        return AiModelResult.builder()
                .fullModelName(model.getFullModelName())
                .hardTokenLimit(model.getHardTokenLimit())
                .internalModelName(model.toString())
                .officialModelName(model.getOfficialModelName())
                .build();
    }
}
