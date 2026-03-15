package me.moirai.storyengine.core.port.outbound.world;

import java.util.Optional;

import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntries;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntriesResult;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldsResult;

public interface WorldRepository {

    World save(World world);

    Optional<World> findById(Long id);

    Optional<World> findByPublicId(String publicId);

    void deleteById(Long id);

    void deleteByPublicId(String publicId);

    SearchWorldsResult search(SearchWorlds request);

    SearchWorldLorebookEntriesResult searchLorebookEntries(SearchWorldLorebookEntries query);
}
