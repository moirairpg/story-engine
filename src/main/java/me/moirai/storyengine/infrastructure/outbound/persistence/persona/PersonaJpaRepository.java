package me.moirai.storyengine.infrastructure.outbound.persistence.persona;

import org.springframework.data.jpa.repository.JpaRepository;

import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.infrastructure.outbound.persistence.PaginationRepository;

public interface PersonaJpaRepository
        extends JpaRepository<Persona, String>, PaginationRepository<Persona, String> {

}
