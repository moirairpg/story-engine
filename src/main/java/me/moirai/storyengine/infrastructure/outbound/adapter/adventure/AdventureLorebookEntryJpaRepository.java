package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import me.moirai.storyengine.common.dbutil.PaginationRepository;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;

public interface AdventureLorebookEntryJpaRepository
        extends JpaRepository<AdventureLorebookEntry, String>,
        PaginationRepository<AdventureLorebookEntry, String> {

    @Query(value = "SELECT entry.* FROM adventure_lorebook entry WHERE :valueToMatch ~ entry.regex AND entry.adventure_id = :adventureId", nativeQuery = true)
    List<AdventureLorebookEntry> findAllByNameRegex(String valueToMatch, String adventureId);

    @Query(value = "SELECT entry.* FROM adventure_lorebook entry WHERE entry.player_id = :playerId AND entry.adventure_id = :adventureId", nativeQuery = true)
    Optional<AdventureLorebookEntry> findByPlayerId(String playerId, String adventureId);

    List<AdventureLorebookEntry> findAllByAdventureId(String adventureId);
}