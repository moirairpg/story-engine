package me.moirai.storyengine.infrastructure.outbound.adapter.persona;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import me.moirai.storyengine.common.dbutil.PaginationRepository;
import me.moirai.storyengine.core.domain.persona.Persona;

public interface PersonaJpaRepository
        extends JpaRepository<Persona, Long>, PaginationRepository<Persona, Long> {

    Optional<Persona> findByPublicId(String publicId);

    void deleteByPublicId(String publicId);

    boolean existsByPublicId(String publicId);
}
