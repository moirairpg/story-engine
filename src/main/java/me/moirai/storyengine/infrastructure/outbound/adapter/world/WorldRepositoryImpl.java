package me.moirai.storyengine.infrastructure.outbound.adapter.world;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@Repository
public class WorldRepositoryImpl implements WorldRepository {

    private final WorldJpaRepository jpaRepository;

    public WorldRepositoryImpl(WorldJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public World save(World world) {

        return jpaRepository.save(world);
    }

    @Override
    public Optional<World> findById(Long id) {

        return jpaRepository.findById(id);
    }

    @Override
    public Optional<World> findByPublicId(UUID publicId) {

        return jpaRepository.findByPublicId(publicId);
    }

    @Override
    public void deleteByPublicId(UUID publicId) {

        jpaRepository.deleteByPublicId(publicId);
    }
}
