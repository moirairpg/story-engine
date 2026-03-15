package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import org.springframework.data.jpa.repository.JpaRepository;

import me.moirai.storyengine.common.dbutil.PaginationRepository;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;

public interface AdventureLorebookEntryJpaRepository
        extends JpaRepository<AdventureLorebookEntry, Long>,
        PaginationRepository<AdventureLorebookEntry, Long> {
}
