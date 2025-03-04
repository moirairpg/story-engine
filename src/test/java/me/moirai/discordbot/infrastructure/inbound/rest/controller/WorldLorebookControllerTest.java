package me.moirai.discordbot.infrastructure.inbound.rest.controller;

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

import me.moirai.discordbot.AbstractRestWebTest;
import me.moirai.discordbot.core.application.usecase.world.request.CreateWorldLorebookEntry;
import me.moirai.discordbot.core.application.usecase.world.request.DeleteWorldLorebookEntry;
import me.moirai.discordbot.core.application.usecase.world.request.GetWorldLorebookEntryById;
import me.moirai.discordbot.core.application.usecase.world.request.SearchWorldLorebookEntries;
import me.moirai.discordbot.core.application.usecase.world.request.UpdateWorldLorebookEntry;
import me.moirai.discordbot.core.application.usecase.world.result.CreateWorldLorebookEntryResult;
import me.moirai.discordbot.core.application.usecase.world.result.GetWorldLorebookEntryResult;
import me.moirai.discordbot.core.application.usecase.world.result.SearchWorldLorebookEntriesResult;
import me.moirai.discordbot.core.application.usecase.world.result.UpdateWorldLorebookEntryResult;
import me.moirai.discordbot.infrastructure.inbound.rest.mapper.WorldLorebookEntryRequestMapper;
import me.moirai.discordbot.infrastructure.inbound.rest.mapper.WorldLorebookEntryResponseMapper;
import me.moirai.discordbot.infrastructure.inbound.rest.request.CreateLorebookEntryRequest;
import me.moirai.discordbot.infrastructure.inbound.rest.request.UpdateLorebookEntryRequest;
import me.moirai.discordbot.infrastructure.inbound.rest.response.CreateLorebookEntryResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.CreateWorldResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.LorebookEntryResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.SearchLorebookEntriesResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.SearchWorldsResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.UpdateLorebookEntryResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.UpdateWorldResponse;
import me.moirai.discordbot.infrastructure.security.authentication.config.AuthenticationSecurityConfig;

@WebFluxTest(controllers = {
        WorldLorebookController.class
}, excludeAutoConfiguration = {
        ReactiveSecurityAutoConfiguration.class,
        AuthenticationSecurityConfig.class
})
public class WorldLorebookControllerTest extends AbstractRestWebTest {

    @MockBean
    protected WorldLorebookEntryResponseMapper worldLorebookEntryResponseMapper;

    @MockBean
    protected WorldLorebookEntryRequestMapper worldLorebookEntryRequestMapper;

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

        when(useCaseRunner.run(any(SearchWorldLorebookEntries.class)))
                .thenReturn(mock(SearchWorldLorebookEntriesResult.class));

        when(worldLorebookEntryResponseMapper.toResponse(any(SearchWorldLorebookEntriesResult.class)))
                .thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri("/world/1234/lorebook")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(SearchWorldsResponse.class)
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

        when(useCaseRunner.run(any(SearchWorldLorebookEntries.class)))
                .thenReturn(mock(SearchWorldLorebookEntriesResult.class));

        when(worldLorebookEntryResponseMapper.toResponse(any(SearchWorldLorebookEntriesResult.class)))
                .thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri(uri -> uri.path("/world/1234/lorebook")
                        .queryParam("name", "someName")
                        .queryParam("page", 1)
                        .queryParam("size", 10)
                        .queryParam("sortingField", "NAME")
                        .queryParam("direction", "ASC")
                        .build())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(SearchWorldsResponse.class)
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

        when(useCaseRunner.run(any(GetWorldLorebookEntryById.class)))
                .thenReturn(mock(GetWorldLorebookEntryResult.class));

        when(worldLorebookEntryResponseMapper.toResponse(any(GetWorldLorebookEntryResult.class)))
                .thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri("/world/1234/lorebook/ID")
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

        when(worldLorebookEntryRequestMapper.toCommand(any(CreateLorebookEntryRequest.class),
                anyString(), anyString())).thenReturn(mock(CreateWorldLorebookEntry.class));

        when(useCaseRunner.run(any(CreateWorldLorebookEntry.class)))
                .thenReturn(mock(CreateWorldLorebookEntryResult.class));

        when(worldLorebookEntryResponseMapper.toResponse(any(CreateWorldLorebookEntryResult.class)))
                .thenReturn(expectedResponse);

        // Then
        webTestClient.post()
                .uri("/world/1234/lorebook")
                .bodyValue(request)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(CreateWorldResponse.class)
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

        when(worldLorebookEntryRequestMapper.toCommand(any(UpdateLorebookEntryRequest.class),
                anyString(), anyString(), anyString())).thenReturn(mock(UpdateWorldLorebookEntry.class));

        when(useCaseRunner.run(any(UpdateWorldLorebookEntry.class)))
                .thenReturn(mock(UpdateWorldLorebookEntryResult.class));

        when(worldLorebookEntryResponseMapper.toResponse(any(UpdateWorldLorebookEntryResult.class)))
                .thenReturn(expectedResponse);

        // Then
        webTestClient.put()
                .uri("/world/1234/lorebook/1234")
                .bodyValue(request)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(UpdateWorldResponse.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getLastUpdateDate()).isEqualTo(expectedResponse.getLastUpdateDate());
                });
    }

    @Test
    public void http200WhenDeleteWorld() {

        // Given
        when(worldLorebookEntryRequestMapper.toCommand(anyString(), anyString(), anyString()))
                .thenReturn(mock(DeleteWorldLorebookEntry.class));

        when(useCaseRunner.run(any(DeleteWorldLorebookEntry.class))).thenReturn(null);

        // Then
        webTestClient.delete()
                .uri("/world/1234/lorebook/1234")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }
}
