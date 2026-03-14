package me.moirai.storyengine.core.domain.world;

import java.util.Optional;

import me.moirai.storyengine.core.application.usecase.world.request.SearchWorlds;
import me.moirai.storyengine.core.application.usecase.world.result.SearchWorldsResult;

public interface WorldRepository {

    World save(World world);

    Optional<World> findById(String id);

    void deleteById(String id);

    SearchWorldsResult search(SearchWorlds request);
}
