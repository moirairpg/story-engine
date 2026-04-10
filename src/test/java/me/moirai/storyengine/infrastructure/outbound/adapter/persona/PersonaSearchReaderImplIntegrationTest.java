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

    private static final Long OWNER_ID = PersonaFixture.OWNER_ID;

    @Autowired
    private PersonaSearchReader reader;

    @BeforeEach
    public void before() {
        clear(Persona.class);
    }

    @Test
    public void search_whenMyStuffAndOwnerExists_thenReturnResults() {

        // Given
        var persona = PersonaFixture.publicPersona().build();
        insert(persona, Persona.class);

        var query = new SearchPersonas(null, SearchView.MY_STUFF, null, null, null, null, OWNER_ID);

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

        var query = new SearchPersonas(null, SearchView.EXPLORE, null, null, null, null, OWNER_ID);

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
        var query = new SearchPersonas(null, SearchView.EXPLORE, null, null, null, null, OWNER_ID);

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
        var persona = PersonaFixture.publicPersona().build();
        insert(persona, Persona.class);

        var query = new SearchPersonas("MoirAI", SearchView.MY_STUFF, null, null, null, null, OWNER_ID);

        // When
        var result = reader.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).isEqualTo(1);
        assertThat(result.data().get(0).name()).isEqualTo("MoirAI");
    }

    @Test
    public void search_whenUserIsOwner_thenUserPermissionIsOwner() {

        // Given
        insert(PersonaFixture.publicPersona().build(), Persona.class);

        var query = new SearchPersonas(null, SearchView.EXPLORE, null, null, null, null, OWNER_ID);

        // When
        var result = reader.search(query);

        // Then
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).userPermission()).isEqualTo("OWNER");
    }

    @Test
    public void search_whenUserHasNoPermission_thenUserPermissionIsNull() {

        // Given
        insert(PersonaFixture.publicPersona().build(), Persona.class);

        var query = new SearchPersonas(null, SearchView.EXPLORE, null, null, null, null, 999999L);

        // When
        var result = reader.search(query);

        // Then
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).userPermission()).isNull();
    }
}
