package me.moirai.storyengine.infrastructure.outbound.adapter.chronicle;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import me.moirai.storyengine.core.domain.chronicle.ChronicleSegment;

public interface ChronicleSegmentJpaRepository extends JpaRepository<ChronicleSegment, Long> {

    @Query("SELECT s FROM ChronicleSegment s WHERE s.publicId IN :publicIds")
    List<ChronicleSegment> findAllByPublicIdIn(@Param("publicIds") List<UUID> publicIds);
}
