package me.moirai.storyengine.infrastructure.outbound.adapter.persona;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.outbound.persona.PersonaReader;

public class PersonaReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PersonaReader reader;

    @BeforeEach
    public void before() {
        clear(Persona.class);
    }

    @Test
    public void getPersonaById_whenNotFound_thenReturnEmpty() {

        // Given
        var publicId = UUID.randomUUID();

        // When
        var result = reader.getPersonaById(publicId);

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    public void getPersonaById_whenFound_thenReturnDetails() {

        // Given
        var persona = PersonaFixture.publicPersona().build();
        insert(persona, Persona.class);

        // When
        var result = reader.getPersonaById(persona.getPublicId());

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result.get().id()).isEqualTo(persona.getPublicId());
        assertThat(result.get().name()).isEqualTo(persona.getName());
        assertThat(result.get().personality()).isEqualTo(persona.getPersonality());
        assertThat(result.get().creationDate()).isNotNull();
        assertThat(result.get().lastUpdateDate()).isNotNull();
    }
}
