package me.moirai.storyengine.infrastructure.outbound.adapter.persona;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import me.moirai.storyengine.common.dbutil.PaginationRepository;
import me.moirai.storyengine.core.domain.persona.Persona;

public interface PersonaJpaRepository
        extends JpaRepository<Persona, Long>, PaginationRepository<Persona, Long> {

    Optional<Persona> findByPublicId(UUID publicId);

    void deleteByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);
}
