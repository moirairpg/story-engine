package me.moirai.storyengine.core.port.inbound.model;

import java.util.List;

import me.moirai.storyengine.common.cqs.query.Query;

public record SearchModels(
        String modelToSearch,
        String tokenLimit)
        implements Query<List<AiModelResult>> {

}
