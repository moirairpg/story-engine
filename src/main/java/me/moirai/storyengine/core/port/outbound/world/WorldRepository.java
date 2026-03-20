package me.moirai.storyengine.core.port.outbound.world;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.domain.world.World;

public interface WorldRepository {

    World save(World world);

    Optional<World> findById(Long id);

    Optional<World> findByPublicId(UUID publicId);

    void deleteByPublicId(UUID publicId);
}
