package me.moirai.storyengine.core.port.inbound.model;

import java.util.List;

import me.moirai.storyengine.common.usecases.UseCase;

public class SearchModels extends UseCase<List<AiModelResult>> {

    private final String modelToSearch;
    private final String tokenLimit;

    private SearchModels(String modelToSearch, String tokenLimit) {
        this.modelToSearch = modelToSearch;
        this.tokenLimit = tokenLimit;
    }

    public static SearchModels build(String modelToSearch, String tokenLimit) {
        return new SearchModels(modelToSearch, tokenLimit);
    }

    public String getModelToSearch() {
        return modelToSearch;
    }

    public String getTokenLimit() {
        return tokenLimit;
    }
}
