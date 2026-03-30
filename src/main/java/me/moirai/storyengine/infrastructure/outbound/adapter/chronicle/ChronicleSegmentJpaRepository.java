package me.moirai.storyengine.infrastructure.outbound.adapter.chronicle;

import org.springframework.data.jpa.repository.JpaRepository;

import me.moirai.storyengine.core.domain.chronicle.ChronicleSegment;

public interface ChronicleSegmentJpaRepository extends JpaRepository<ChronicleSegment, Long> {
}
