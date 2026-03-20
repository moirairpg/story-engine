package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;

@ExtendWith(MockitoExtension.class)
public class PersonaPersistenceMapperTest {

    @InjectMocks
    private PersonaPersistenceMapper mapper;

    @Test
    public void mapPersonaDomain_whenGetOperation_thenMapToGetResult() {

        var persona = PersonaFixture.privatePersonaWithId();

        PersonaDetails result = mapper.mapToResult(persona);

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
}
