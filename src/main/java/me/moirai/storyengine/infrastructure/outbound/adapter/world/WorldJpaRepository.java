package me.moirai.storyengine.infrastructure.outbound.adapter.world;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import me.moirai.storyengine.common.dbutil.PaginationRepository;
import me.moirai.storyengine.core.domain.world.World;

public interface WorldJpaRepository
        extends JpaRepository<World, Long>, PaginationRepository<World, Long> {

    Optional<World> findByPublicId(String publicId);

    void deleteByPublicId(String publicId);
}