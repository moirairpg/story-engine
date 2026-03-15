package me.moirai.storyengine.core.port.outbound.world;

import java.util.Optional;

import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntries;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntriesResult;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldsResult;

public interface WorldRepository {

    World save(World world);

    Optional<World> findById(String id);

    void deleteById(String id);

    SearchWorldsResult search(SearchWorlds request);

    SearchWorldLorebookEntriesResult searchLorebookEntries(SearchWorldLorebookEntries query);
}
