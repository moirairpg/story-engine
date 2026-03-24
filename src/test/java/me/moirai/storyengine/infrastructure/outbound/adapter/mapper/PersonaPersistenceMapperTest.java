package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.domain.persona.PersonaFixture;

@ExtendWith(MockitoExtension.class)
public class PersonaPersistenceMapperTest {

    @InjectMocks
    private PersonaPersistenceMapper mapper;

    @Test
    public void mapPersonaDomain_whenGetOperation_thenMapToGetResult() {

        // given
        var persona = PersonaFixture.privatePersonaWithId();

        // when
        var result = mapper.mapToResult(persona);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(persona.getPublicId());
        assertThat(result.name()).isEqualTo(persona.getName());
        assertThat(result.personality()).isEqualTo(persona.getPersonality());
        assertThat(result.visibility()).isEqualTo(persona.getVisibility());
        assertThat(result.permissions()).hasSameElementsAs(persona.getPermissions());
        assertThat(result.creationDate()).isEqualTo(persona.getCreationDate());
        assertThat(result.lastUpdateDate()).isEqualTo(persona.getLastUpdateDate());
    }
}
