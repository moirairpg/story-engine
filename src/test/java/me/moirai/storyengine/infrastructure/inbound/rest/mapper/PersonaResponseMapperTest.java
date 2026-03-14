package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.persona.result.CreatePersonaResult;
import me.moirai.storyengine.core.application.usecase.persona.result.GetPersonaResult;
import me.moirai.storyengine.core.application.usecase.persona.result.GetPersonaResultFixture;
import me.moirai.storyengine.core.application.usecase.persona.result.SearchPersonasResult;
import me.moirai.storyengine.core.application.usecase.persona.result.UpdatePersonaResult;
import me.moirai.storyengine.infrastructure.inbound.rest.response.CreatePersonaResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.PersonaResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.SearchPersonasResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.UpdatePersonaResponse;

@ExtendWith(MockitoExtension.class)
public class PersonaResponseMapperTest {

    @InjectMocks
    private PersonaResponseMapper mapper;

    @Test
    public void searchPersonaResultToResponse() {

        // Given
        List<GetPersonaResult> results = Lists.list(GetPersonaResultFixture.privatePersona().build(),
                GetPersonaResultFixture.privatePersona().build());

        SearchPersonasResult result = SearchPersonasResult.builder()
                .page(1)
                .totalPages(2)
                .totalItems(20)
                .items(10)
                .results(results)
                .build();

        // When
        SearchPersonasResponse response = mapper.toResponse(result);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPage()).isEqualTo(result.getPage());
        assertThat(response.getTotalPages()).isEqualTo(result.getTotalPages());
        assertThat(response.getResultsInPage()).isEqualTo(result.getItems());
        assertThat(response.getTotalResults()).isEqualTo(result.getTotalItems());
    }

    @Test
    public void getPersonaResultToResponse() {

        // Given
        GetPersonaResult result = GetPersonaResultFixture.privatePersona().build();

        // When
        PersonaResponse response = mapper.toResponse(result);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(result.getId());
        assertThat(response.getName()).isEqualTo(result.getName());
        assertThat(response.getPersonality()).isEqualTo(result.getPersonality());
        assertThat(response.getVisibility()).isEqualTo(result.getVisibility());
        assertThat(response.getOwnerId()).isEqualTo(result.getOwnerId());
        assertThat(response.getCreationDate()).isEqualTo(result.getCreationDate());
        assertThat(response.getLastUpdateDate()).isEqualTo(result.getLastUpdateDate());
    }

    @Test
    public void createPersonaResultToResponse() {

        // Given
        CreatePersonaResult result = CreatePersonaResult.build("WRLDID");

        // When
        CreatePersonaResponse response = mapper.toResponse(result);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(result.getId());
    }

    @Test
    public void updatePersonaResultToResponse() {

        // Given
        UpdatePersonaResult result = UpdatePersonaResult.build(OffsetDateTime.now());

        // When
        UpdatePersonaResponse response = mapper.toResponse(result);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getLastUpdateDate()).isEqualTo(result.getLastUpdatedDateTime());
    }
}
