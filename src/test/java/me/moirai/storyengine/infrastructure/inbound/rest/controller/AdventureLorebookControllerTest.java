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
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntryResult;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryResult;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntriesResult;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntryResult;
import me.moirai.storyengine.infrastructure.inbound.rest.mapper.AdventureLorebookEntryRequestMapper;
import me.moirai.storyengine.infrastructure.inbound.rest.mapper.AdventureLorebookEntryResponseMapper;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateLorebookEntryRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateLorebookEntryRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.response.CreateAdventureResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.CreateLorebookEntryResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.LorebookEntryResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.SearchAdventuresResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.SearchLorebookEntriesResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.UpdateAdventureResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.UpdateLorebookEntryResponse;
import me.moirai.storyengine.infrastructure.security.authentication.config.AuthenticationSecurityConfig;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = {
        AdventureLorebookController.class
}, excludeAutoConfiguration = {
        ReactiveSecurityAutoConfiguration.class,
        AuthenticationSecurityConfig.class
})
public class AdventureLorebookControllerTest extends AbstractRestWebTest {

    @MockBean
    protected AdventureLorebookEntryResponseMapper adventureLorebookEntryResponseMapper;

    @MockBean
    protected AdventureLorebookEntryRequestMapper adventureLorebookEntryRequestMapper;

    @Test
    public void http200WhenSearchLorebookEntries() {

        // Given
        List<LorebookEntryResponse> results = Lists.list(LorebookEntryResponse.builder()
                .id("ID")
                .name("NAME")
                .description("DESC")
                .regex("regex")
                .creationDate(OffsetDateTime.now())
                .lastUpdateDate(OffsetDateTime.now())
                .build());

        SearchLorebookEntriesResponse expectedResponse = SearchLorebookEntriesResponse.builder()
                .page(1)
                .totalPages(2)
                .totalResults(20)
                .resultsInPage(10)
                .results(results)
                .build();

        when(useCaseRunner.run(any(SearchAdventureLorebookEntries.class)))
                .thenReturn(mock(SearchAdventureLorebookEntriesResult.class));

        when(adventureLorebookEntryResponseMapper.toResponse(any(SearchAdventureLorebookEntriesResult.class)))
                .thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri("/adventure/1234/lorebook")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(SearchAdventuresResponse.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getTotalPages()).isEqualTo(2);
                    assertThat(response.getTotalResults()).isEqualTo(20);
                    assertThat(response.getResultsInPage()).isEqualTo(10);
                    assertThat(response.getResults()).hasSameSizeAs(results);
                });
    }

    @Test
    public void http200WhenSearchLorebookEntriesWithParameters() {

        // Given
        List<LorebookEntryResponse> results = Lists.list(LorebookEntryResponse.builder()
                .id("ID")
                .name("NAME")
                .description("DESC")
                .regex("regex")
                .creationDate(OffsetDateTime.now())
                .lastUpdateDate(OffsetDateTime.now())
                .build());

        SearchLorebookEntriesResponse expectedResponse = SearchLorebookEntriesResponse.builder()
                .page(1)
                .totalPages(2)
                .totalResults(20)
                .resultsInPage(10)
                .results(results)
                .build();

        when(useCaseRunner.run(any(SearchAdventureLorebookEntries.class)))
                .thenReturn(mock(SearchAdventureLorebookEntriesResult.class));

        when(adventureLorebookEntryResponseMapper.toResponse(any(SearchAdventureLorebookEntriesResult.class)))
                .thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri(uri -> uri.path("/adventure/1234/lorebook")
                        .queryParam("name", "someName")
                        .queryParam("page", 1)
                        .queryParam("size", 10)
                        .queryParam("sortingField", "NAME")
                        .queryParam("direction", "ASC")
                        .build())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(SearchAdventuresResponse.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getTotalPages()).isEqualTo(2);
                    assertThat(response.getTotalResults()).isEqualTo(20);
                    assertThat(response.getResultsInPage()).isEqualTo(10);
                    assertThat(response.getResults()).hasSameSizeAs(results);
                });
    }

    @Test
    public void http200WhenGetLorebookEntryById() {

        // Given
        LorebookEntryResponse expectedResponse = LorebookEntryResponse.builder()
                .id("ID")
                .name("NAME")
                .description("DESC")
                .regex("regex")
                .creationDate(OffsetDateTime.now())
                .lastUpdateDate(OffsetDateTime.now())
                .build();

        when(useCaseRunner.run(any(GetAdventureLorebookEntryById.class)))
                .thenReturn(mock(GetAdventureLorebookEntryResult.class));

        when(adventureLorebookEntryResponseMapper.toResponse(any(GetAdventureLorebookEntryResult.class)))
                .thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri("/adventure/1234/lorebook/ID")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(LorebookEntryResponse.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getId()).isEqualTo(expectedResponse.getId());
                    assertThat(response.getName()).isEqualTo(expectedResponse.getName());
                    assertThat(response.getRegex()).isEqualTo(expectedResponse.getRegex());
                    assertThat(response.getDescription()).isEqualTo(expectedResponse.getDescription());
                    assertThat(response.getCreationDate()).isEqualTo(expectedResponse.getCreationDate());
                    assertThat(response.getLastUpdateDate()).isEqualTo(expectedResponse.getLastUpdateDate());
                });
    }

    @Test
    public void http201WhenCreateLorebookEntry() {

        // Given
        CreateLorebookEntryRequest request = new CreateLorebookEntryRequest();
        request.setName("NAME");
        request.setDescription("DESC");
        request.setRegex("regex");

        CreateLorebookEntryResponse expectedResponse = CreateLorebookEntryResponse.build("ID");

        when(adventureLorebookEntryRequestMapper.toCommand(any(CreateLorebookEntryRequest.class),
                anyString(), anyString())).thenReturn(mock(CreateAdventureLorebookEntry.class));

        when(useCaseRunner.run(any(CreateAdventureLorebookEntry.class)))
                .thenReturn(Mono.just(mock(CreateAdventureLorebookEntryResult.class)));

        when(adventureLorebookEntryResponseMapper.toResponse(any(CreateAdventureLorebookEntryResult.class)))
                .thenReturn(expectedResponse);

        // Then
        webTestClient.post()
                .uri("/adventure/1234/lorebook")
                .bodyValue(request)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(CreateAdventureResponse.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getId()).isEqualTo(expectedResponse.getId());
                });
    }

    @Test
    public void http200WhenUpdateLorebookEntry() {

        // Given
        UpdateLorebookEntryRequest request = new UpdateLorebookEntryRequest();
        request.setName("NAME");
        request.setDescription("DESC");
        request.setRegex("regex");

        UpdateLorebookEntryResponse expectedResponse = UpdateLorebookEntryResponse
                .build(OffsetDateTime.now());

        when(adventureLorebookEntryRequestMapper.toCommand(any(UpdateLorebookEntryRequest.class),
                anyString(), anyString(), anyString())).thenReturn(mock(UpdateAdventureLorebookEntry.class));

        when(useCaseRunner.run(any(UpdateAdventureLorebookEntry.class)))
                .thenReturn(Mono.just(mock(UpdateAdventureLorebookEntryResult.class)));

        when(adventureLorebookEntryResponseMapper.toResponse(any(UpdateAdventureLorebookEntryResult.class)))
                .thenReturn(expectedResponse);

        // Then
        webTestClient.put()
                .uri("/adventure/1234/lorebook/1234")
                .bodyValue(request)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(UpdateAdventureResponse.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getLastUpdateDate()).isEqualTo(expectedResponse.getLastUpdateDate());
                });
    }

    @Test
    public void http200WhenDeleteAdventure() {

        // Given
        when(adventureLorebookEntryRequestMapper.toCommand(anyString(), anyString(), anyString()))
                .thenReturn(mock(DeleteAdventureLorebookEntry.class));

        when(useCaseRunner.run(any(DeleteAdventureLorebookEntry.class))).thenReturn(null);

        // Then
        webTestClient.delete()
                .uri("/adventure/1234/lorebook/1234")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }
}
