package me.moirai.storyengine.core.application.query.model;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Arrays;
import java.util.List;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.core.port.inbound.model.AiModelResult;
import me.moirai.storyengine.core.port.inbound.model.SearchModels;

@QueryHandler
public class SearchModelsHandler extends AbstractQueryHandler<SearchModels, List<AiModelResult>> {

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

        if (isEmpty(useCase.modelToSearch())) {
            return true;
        }

        return aiModel.fullModelName().contains(useCase.modelToSearch()) ||
                aiModel.internalModelName().contains(useCase.modelToSearch()) ||
                aiModel.officialModelName().contains(useCase.modelToSearch());
    }

    private boolean matchTokenLimit(SearchModels useCase, AiModelResult aiModel) {

        if (isEmpty(useCase.tokenLimit())) {
            return true;
        }

        return aiModel.hardTokenLimit() == Long.valueOf(useCase.tokenLimit());
    }

    private AiModelResult toResult(ArtificialIntelligenceModel model) {

        return new AiModelResult(
                model.getFullModelName(),
                model.toString(),
                model.getOfficialModelName(),
                model.getHardTokenLimit());
    }
}
