package me.moirai.storyengine.infrastructure.outbound.adapter.chronicle;

import org.springframework.stereotype.Repository;

import me.moirai.storyengine.core.domain.chronicle.ChronicleSegment;
import me.moirai.storyengine.core.port.outbound.chronicle.ChronicleSegmentRepository;

@Repository
public class ChronicleSegmentRepositoryImpl implements ChronicleSegmentRepository {

    private final ChronicleSegmentJpaRepository jpaRepository;

    public ChronicleSegmentRepositoryImpl(ChronicleSegmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ChronicleSegment save(ChronicleSegment segment) {
        return jpaRepository.save(segment);
    }
}
