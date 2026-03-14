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
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntriesResult;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntry;
import me.moirai.storyengine.infrastructure.inbound.rest.mapper.AdventureLorebookEntryRequestMapper;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateLorebookEntryRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateLorebookEntryRequest;
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
    protected AdventureLorebookEntryRequestMapper adventureLorebookEntryRequestMapper;

    @Test
    public void http200WhenSearchLorebookEntries() {

        // Given
        List<AdventureLorebookEntryDetails> results = Lists.list(AdventureLorebookEntryDetails.builder()
                .id("ID")
                .name("NAME")
                .description("DESC")
                .regex("regex")
                .creationDate(OffsetDateTime.now())
                .lastUpdateDate(OffsetDateTime.now())
                .build());

        SearchAdventureLorebookEntriesResult expectedResponse = SearchAdventureLorebookEntriesResult.builder()
                .page(1)
                .totalPages(2)
                .totalItems(20)
                .items(10)
                .results(results)
                .build();

        when(useCaseRunner.run(any(SearchAdventureLorebookEntries.class))).thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri("/adventure/1234/lorebook")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(SearchAdventureLorebookEntriesResult.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getTotalPages()).isEqualTo(2);
                    assertThat(response.getTotalItems()).isEqualTo(20);
                    assertThat(response.getItems()).isEqualTo(10);
                    assertThat(response.getResults()).hasSameSizeAs(results);
                });
    }

    @Test
    public void http200WhenSearchLorebookEntriesWithParameters() {

        // Given
        List<AdventureLorebookEntryDetails> results = Lists.list(AdventureLorebookEntryDetails.builder()
                .id("ID")
                .name("NAME")
                .description("DESC")
                .regex("regex")
                .creationDate(OffsetDateTime.now())
                .lastUpdateDate(OffsetDateTime.now())
                .build());

        SearchAdventureLorebookEntriesResult expectedResponse = SearchAdventureLorebookEntriesResult.builder()
                .page(1)
                .totalPages(2)
                .totalItems(20)
                .items(10)
                .results(results)
                .build();

        when(useCaseRunner.run(any(SearchAdventureLorebookEntries.class))).thenReturn(expectedResponse);

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
                .expectBody(SearchAdventureLorebookEntriesResult.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getTotalPages()).isEqualTo(2);
                    assertThat(response.getTotalItems()).isEqualTo(20);
                    assertThat(response.getItems()).isEqualTo(10);
                    assertThat(response.getResults()).hasSameSizeAs(results);
                });
    }

    @Test
    public void http200WhenGetLorebookEntryById() {

        // Given
        AdventureLorebookEntryDetails expectedResponse = AdventureLorebookEntryDetails.builder()
                .id("ID")
                .name("NAME")
                .description("DESC")
                .regex("regex")
                .creationDate(OffsetDateTime.now())
                .lastUpdateDate(OffsetDateTime.now())
                .build();

        when(useCaseRunner.run(any(GetAdventureLorebookEntryById.class))).thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri("/adventure/1234/lorebook/ID")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(AdventureLorebookEntryDetails.class)
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

        AdventureLorebookEntryDetails expectedResponse = AdventureLorebookEntryDetails.builder().id("ID").build();

        when(adventureLorebookEntryRequestMapper.toCommand(any(CreateLorebookEntryRequest.class),
                anyString(), anyString())).thenReturn(mock(CreateAdventureLorebookEntry.class));

        when(useCaseRunner.run(any(CreateAdventureLorebookEntry.class)))
                .thenReturn(Mono.just(expectedResponse));

        // Then
        webTestClient.post()
                .uri("/adventure/1234/lorebook")
                .bodyValue(request)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(AdventureLorebookEntryDetails.class)
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

        AdventureLorebookEntryDetails expectedResponse = AdventureLorebookEntryDetails.builder()
                .lastUpdateDate(OffsetDateTime.now()).build();

        when(adventureLorebookEntryRequestMapper.toCommand(any(UpdateLorebookEntryRequest.class),
                anyString(), anyString(), anyString())).thenReturn(mock(UpdateAdventureLorebookEntry.class));

        when(useCaseRunner.run(any(UpdateAdventureLorebookEntry.class)))
                .thenReturn(Mono.just(expectedResponse));

        // Then
        webTestClient.put()
                .uri("/adventure/1234/lorebook/1234")
                .bodyValue(request)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(AdventureLorebookEntryDetails.class)
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
