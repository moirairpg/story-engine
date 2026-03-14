package me.moirai.storyengine.infrastructure.outbound.persistence.world;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.infrastructure.outbound.persistence.PaginationRepository;

public interface WorldLorebookEntryJpaRepository
        extends JpaRepository<WorldLorebookEntry, String>,
        PaginationRepository<WorldLorebookEntry, String> {

    @Query(value = "SELECT entry.* FROM world_lorebook entry WHERE :valueToMatch ~ entry.regex AND entry.world_id = :worldId", nativeQuery = true)
    List<WorldLorebookEntry> findAllByNameRegex(String valueToMatch, String worldId);

    List<WorldLorebookEntry> findAllByWorldId(String worldId);
}