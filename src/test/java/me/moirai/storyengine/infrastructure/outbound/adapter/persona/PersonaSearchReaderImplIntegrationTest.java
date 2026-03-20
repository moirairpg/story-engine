package me.moirai.storyengine.infrastructure.outbound.adapter.persona;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.common.enums.SearchView;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;
import me.moirai.storyengine.core.port.outbound.persona.PersonaSearchReader;

public class PersonaSearchReaderImplIntegrationTest extends AbstractIntegrationTest {

    private static final String OWNER_ID = "586678721356875";

    @Autowired
    private PersonaSearchReader reader;

    @BeforeEach
    public void before() {
        clear(Persona.class);
    }

    @Test
    public void search_whenMyStuffAndOwnerExists_thenReturnResults() {

        // Given
        insert(PersonaFixture.publicPersona().build(), Persona.class);

        var query = new SearchPersonas(null, null, SearchView.MY_STUFF, null, null, null, null, OWNER_ID);

        // When
        var result = reader.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).isEqualTo(1);
        assertThat(result.data()).hasSize(1);
    }

    @Test
    public void search_whenExploreAndPublicPersonaExists_thenReturnResults() {

        // Given
        insert(PersonaFixture.publicPersona().build(), Persona.class);

        var query = new SearchPersonas(null, null, SearchView.EXPLORE, null, null, null, null, OWNER_ID);

        // When
        var result = reader.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).isEqualTo(1);
        assertThat(result.data()).hasSize(1);
    }

    @Test
    public void search_whenNoResults_thenReturnEmpty() {

        // Given
        var query = new SearchPersonas(null, null, SearchView.EXPLORE, null, null, null, null, OWNER_ID);

        // When
        var result = reader.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).isEqualTo(0);
        assertThat(result.data()).isEmpty();
    }

    @Test
    public void search_whenFilterByName_thenReturnMatchingResults() {

        // Given
        insert(PersonaFixture.publicPersona().build(), Persona.class);

        var query = new SearchPersonas("MoirAI", null, SearchView.MY_STUFF, null, null, null, null, OWNER_ID);

        // When
        var result = reader.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).isEqualTo(1);
        assertThat(result.data().get(0).name()).isEqualTo("MoirAI");
    }
}
