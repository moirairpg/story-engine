package me.moirai.storyengine.infrastructure.outbound.adapter.persona;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

@Repository
public class PersonaRepositoryImpl implements PersonaRepository {

    private final PersonaJpaRepository jpaRepository;

    public PersonaRepositoryImpl(PersonaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Persona save(Persona persona) {

        return jpaRepository.save(persona);
    }

    @Override
    public Optional<Persona> findById(Long id) {

        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Persona> findByPublicId(UUID publicId) {

        return jpaRepository.findByPublicId(publicId);
    }

    @Override
    public void deleteByPublicId(UUID publicId) {

        jpaRepository.deleteByPublicId(publicId);
    }
}
