package me.moirai.storyengine.infrastructure.outbound.adapter.chronicle;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import me.moirai.storyengine.core.domain.chronicle.ChronicleSegment;

public interface ChronicleSegmentJpaRepository extends JpaRepository<ChronicleSegment, Long> {

    @Query("SELECT s FROM ChronicleSegment s WHERE s.publicId IN :publicIds")
    List<ChronicleSegment> findAllByPublicIdIn(@Param("publicIds") List<UUID> publicIds);

    @Modifying
    @Query("""
            DELETE FROM ChronicleSegment s
             WHERE s.adventureId = (
                   SELECT a.id FROM Adventure a WHERE a.publicId = :adventurePublicId
                   )
            """)
    void deleteAllByAdventurePublicId(@Param("adventurePublicId") UUID adventurePublicId);
}
