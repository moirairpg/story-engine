package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import me.moirai.storyengine.AbstractRestWebTest;
import me.moirai.storyengine.core.port.inbound.persona.AddFavoritePersona;
import me.moirai.storyengine.core.port.inbound.persona.CreatePersona;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.inbound.persona.DeletePersona;
import me.moirai.storyengine.core.port.inbound.persona.GetPersonaById;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.inbound.persona.RemoveFavoritePersona;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonasResult;
import me.moirai.storyengine.core.port.inbound.persona.UpdatePersona;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.application.usecase.persona.result.GetPersonaResultFixture;
import me.moirai.storyengine.infrastructure.inbound.rest.mapper.PersonaRequestMapper;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreatePersonaRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreatePersonaRequestFixture;
import me.moirai.storyengine.infrastructure.inbound.rest.request.FavoriteRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdatePersonaRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdatePersonaRequestFixture;
import me.moirai.storyengine.infrastructure.security.authentication.config.AuthenticationSecurityConfig;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = {
        PersonaController.class
}, excludeAutoConfiguration = {
        ReactiveSecurityAutoConfiguration.class,
        AuthenticationSecurityConfig.class
})
public class PersonaControllerTest extends AbstractRestWebTest {

    private static final String PERSONA_BASE_URL = "/persona";
    private static final String PERSONA_ID_BASE_URL = "/persona/%s";

    @MockBean
    private PersonaRequestMapper personaRequestMapper;

    @Test
    public void http200WhenSearchPersonas() {

        // Given
        List<PersonaDetails> results = Lists.list(GetPersonaResultFixture.publicPersona().build(),
                GetPersonaResultFixture.privatePersona().build());

        SearchPersonasResult expectedResponse = SearchPersonasResult.builder()
                .page(1)
                .totalPages(2)
                .totalItems(20)
                .items(10)
                .results(results)
                .build();

        when(useCaseRunner.run(any(SearchPersonas.class))).thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri(String.format(PERSONA_BASE_URL))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(SearchPersonasResult.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getTotalPages()).isEqualTo(2);
                    assertThat(response.getTotalItems()).isEqualTo(20);
                    assertThat(response.getItems()).isEqualTo(10);
                    assertThat(response.getResults()).hasSameSizeAs(results);
                });
    }

    @Test
    public void http200WhenSearchPersonasWithParameters() {

        // Given
        List<PersonaDetails> results = Lists.list(GetPersonaResultFixture.publicPersona().build(),
                GetPersonaResultFixture.privatePersona().build());

        SearchPersonasResult expectedResponse = SearchPersonasResult.builder()
                .page(1)
                .totalPages(2)
                .totalItems(20)
                .items(10)
                .results(results)
                .build();

        when(useCaseRunner.run(any(SearchPersonas.class))).thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri(uri -> uri.path(String.format(PERSONA_BASE_URL))
                        .queryParam("name", "someName")
                        .queryParam("ownerId", "someName")
                        .queryParam("favorites", true)
                        .queryParam("page", 1)
                        .queryParam("size", 10)
                        .queryParam("sortingField", "NAME")
                        .queryParam("direction", "ASC")
                        .queryParam("visibility", "PRIVATE")
                        .queryParam("operation", "WRITE")
                        .build())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(SearchPersonasResult.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getTotalPages()).isEqualTo(2);
                    assertThat(response.getTotalItems()).isEqualTo(20);
                    assertThat(response.getItems()).isEqualTo(10);
                    assertThat(response.getResults()).hasSameSizeAs(results);
                });
    }

    @Test
    public void http200WhenGetPersonaById() {

        // Given
        String personaId = "WRLDID";

        PersonaDetails expectedResponse = GetPersonaResultFixture.publicPersona().build();

        when(useCaseRunner.run(any(GetPersonaById.class))).thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri(String.format(PERSONA_ID_BASE_URL, personaId))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(PersonaDetails.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getId()).isEqualTo(expectedResponse.getId());
                    assertThat(response.getName()).isEqualTo(expectedResponse.getName());
                    assertThat(response.getVisibility()).isEqualTo(expectedResponse.getVisibility());
                    assertThat(response.getOwnerId()).isEqualTo(expectedResponse.getOwnerId());
                    assertThat(response.getCreationDate()).isEqualTo(expectedResponse.getCreationDate());
                    assertThat(response.getLastUpdateDate()).isEqualTo(expectedResponse.getLastUpdateDate());

                    assertThat(response.getUsersAllowedToRead())
                            .hasSameElementsAs(expectedResponse.getUsersAllowedToRead());

                    assertThat(response.getUsersAllowedToWrite())
                            .hasSameElementsAs(expectedResponse.getUsersAllowedToWrite());
                });
    }

    @Test
    public void http201WhenCreatePersona() {

        // Given
        CreatePersonaRequest request = CreatePersonaRequestFixture.createPrivatePersona();
        PersonaDetails expectedResponse = PersonaDetails.builder().id("WRLDID").build();

        when(personaRequestMapper.toCommand(any(CreatePersonaRequest.class), anyString()))
                .thenReturn(mock(CreatePersona.class));

        when(useCaseRunner.run(any(CreatePersona.class))).thenReturn(Mono.just(expectedResponse));

        // Then
        webTestClient.post()
                .uri(PERSONA_BASE_URL)
                .bodyValue(request)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(PersonaDetails.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getId()).isEqualTo(expectedResponse.getId());
                });
    }

    @Test
    public void http200WhenUpdatePersona() {

        // Given
        String personaId = "WRLDID";
        UpdatePersonaRequest request = UpdatePersonaRequestFixture.privatePersona();
        PersonaDetails expectedResponse = PersonaDetails.builder().lastUpdateDate(OffsetDateTime.now()).build();

        when(personaRequestMapper.toCommand(any(UpdatePersonaRequest.class), anyString(), anyString()))
                .thenReturn(mock(UpdatePersona.class));

        when(useCaseRunner.run(any(UpdatePersona.class))).thenReturn(Mono.just(expectedResponse));

        // Then
        webTestClient.put()
                .uri(String.format(PERSONA_ID_BASE_URL, personaId))
                .bodyValue(request)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(PersonaDetails.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getLastUpdateDate()).isEqualTo(expectedResponse.getLastUpdateDate());
                });
    }

    @Test
    public void http200WhenDeletePersona() {

        // Given
        String personaId = "WRLDID";

        when(useCaseRunner.run(any(DeletePersona.class))).thenReturn(null);

        // Then
        webTestClient.delete()
                .uri(String.format(PERSONA_ID_BASE_URL, personaId))
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    public void http201WhenAddFavoritePersona() {

        // Given
        FavoriteRequest request = new FavoriteRequest();
        request.setAssetId("1234");

        when(useCaseRunner.run(any(AddFavoritePersona.class))).thenReturn(null);

        // Then
        webTestClient.post()
                .uri(String.format(PERSONA_ID_BASE_URL, "favorite"))
                .bodyValue(request)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    public void http201WhenRemoveFavoritePersona() {

        // Given
        FavoriteRequest request = new FavoriteRequest();
        request.setAssetId("1234");

        when(useCaseRunner.run(any(RemoveFavoritePersona.class))).thenReturn(null);

        // Then
        webTestClient.delete()
                .uri(String.format(PERSONA_ID_BASE_URL, "favorite/1234"))
                .exchange()
                .expectStatus().is2xxSuccessful();
    }
}
