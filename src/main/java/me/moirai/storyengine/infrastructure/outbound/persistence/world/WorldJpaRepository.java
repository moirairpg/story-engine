package me.moirai.storyengine.infrastructure.outbound.persistence.world;

import org.springframework.data.jpa.repository.JpaRepository;

import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.infrastructure.outbound.persistence.PaginationRepository;

public interface WorldJpaRepository
        extends JpaRepository<World, String>, PaginationRepository<World, String> {

}