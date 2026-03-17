package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonasResult;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;

@ExtendWith(MockitoExtension.class)
public class PersonaPersistenceMapperTest {

    @InjectMocks
    private PersonaPersistenceMapper mapper;

    @Test
    public void mapPersonaDomain_whenGetOperation_thenMapToGetResult() {

        // Given
        Persona persona = PersonaFixture.privatePersonaWithId();

        // When
        PersonaDetails result = mapper.mapToResult(persona);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(persona.getPublicId());
        assertThat(result.name()).isEqualTo(persona.getName());
        assertThat(result.personality()).isEqualTo(persona.getPersonality());
        assertThat(result.visibility()).isEqualTo(persona.getVisibility());
        assertThat(result.usersAllowedToRead()).hasSameElementsAs(persona.getUsersAllowedToRead());
        assertThat(result.usersAllowedToWrite()).hasSameElementsAs(persona.getUsersAllowedToWrite());
        assertThat(result.creationDate()).isEqualTo(persona.getCreationDate());
        assertThat(result.lastUpdateDate()).isEqualTo(persona.getLastUpdateDate());
        assertThat(result.ownerId()).isEqualTo(persona.getOwnerId());
    }

    @Test
    public void mapPersonaDomain_whenSearchPersona_thenMapToServer() {

        // Given
        List<Persona> personas = IntStream.range(0, 20)
                .mapToObj(op -> PersonaFixture.privatePersona().build())
                .toList();

        Pageable pageable = Pageable.ofSize(10);
        Page<Persona> page = new PageImpl<>(personas, pageable, 20);

        // When
        SearchPersonasResult result = mapper.mapToResult(page);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.items()).isEqualTo(20);
    }
}
