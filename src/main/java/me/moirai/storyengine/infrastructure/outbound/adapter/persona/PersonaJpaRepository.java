package me.moirai.storyengine.infrastructure.outbound.adapter.persona;

import org.springframework.data.jpa.repository.JpaRepository;

import me.moirai.storyengine.common.dbutil.PaginationRepository;
import me.moirai.storyengine.core.domain.persona.Persona;

public interface PersonaJpaRepository
        extends JpaRepository<Persona, String>, PaginationRepository<Persona, String> {

}
