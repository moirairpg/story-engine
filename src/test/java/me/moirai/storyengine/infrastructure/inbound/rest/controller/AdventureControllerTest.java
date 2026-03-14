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
import me.moirai.storyengine.core.application.usecase.adventure.result.GetAdventureResultFixture;
import me.moirai.storyengine.core.port.inbound.adventure.AddFavoriteAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureById;
import me.moirai.storyengine.core.port.inbound.adventure.RemoveFavoriteAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventuresResult;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;
import me.moirai.storyengine.infrastructure.inbound.rest.mapper.AdventureRequestMapper;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateAdventureRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateAdventureRequestFixture;
import me.moirai.storyengine.infrastructure.inbound.rest.request.FavoriteRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateAdventureRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateAdventureRequestFixture;
import me.moirai.storyengine.infrastructure.security.authentication.config.AuthenticationSecurityConfig;

@WebFluxTest(controllers = {
        AdventureController.class
}, excludeAutoConfiguration = {
        ReactiveSecurityAutoConfiguration.class,
        AuthenticationSecurityConfig.class
})
public class AdventureControllerTest extends AbstractRestWebTest {

    private static final String ADVENTURE_BASE_URL = "/adventure";
    private static final String ADVENTURE_ID_BASE_URL = "/adventure/%s";

    @MockBean
    private AdventureRequestMapper adventureRequestMapper;

    @Test
    public void http200WhenSearchAdventures() {

        // Given
        List<AdventureDetails> results = Lists.list(GetAdventureResultFixture.privateMultiplayerAdventure().build(),
                GetAdventureResultFixture.privateMultiplayerAdventure().build());

        SearchAdventuresResult expectedResponse = SearchAdventuresResult.builder()
                .page(1)
                .totalPages(2)
                .totalItems(20)
                .items(10)
                .results(results)
                .build();

        when(useCaseRunner.run(any(SearchAdventures.class))).thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri(String.format(ADVENTURE_BASE_URL))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(SearchAdventuresResult.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getTotalPages()).isEqualTo(2);
                    assertThat(response.getTotalItems()).isEqualTo(20);
                    assertThat(response.getItems()).isEqualTo(10);
                    assertThat(response.getResults()).hasSameSizeAs(results);
                });
    }

    @Test
    public void http200WhenSearchAdventuresWithParameters() {

        // Given
        List<AdventureDetails> results = Lists.list(GetAdventureResultFixture.privateMultiplayerAdventure().build(),
                GetAdventureResultFixture.privateMultiplayerAdventure().build());

        SearchAdventuresResult expectedResponse = SearchAdventuresResult.builder()
                .page(1)
                .totalPages(2)
                .totalItems(20)
                .items(10)
                .results(results)
                .build();

        when(useCaseRunner.run(any(SearchAdventures.class))).thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri(uri -> uri.path(String.format(ADVENTURE_BASE_URL))
                        .queryParam("name", "someName")
                        .queryParam("world", "someName")
                        .queryParam("persona", "someName")
                        .queryParam("ownerId", "someName")
                        .queryParam("favorites", true)
                        .queryParam("isMultiplayer", false)
                        .queryParam("page", 1)
                        .queryParam("size", 10)
                        .queryParam("model", "GPT35_TURBO")
                        .queryParam("gameMode", "RPG")
                        .queryParam("moderation", "STRICT")
                        .queryParam("sortingField", "NAME")
                        .queryParam("direction", "ASC")
                        .queryParam("visibility", "PRIVATE")
                        .queryParam("operation", "WRITE")
                        .build())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(SearchAdventuresResult.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getTotalPages()).isEqualTo(2);
                    assertThat(response.getTotalItems()).isEqualTo(20);
                    assertThat(response.getItems()).isEqualTo(10);
                    assertThat(response.getResults()).hasSameSizeAs(results);
                });
    }

    @Test
    public void http200WhenGetAdventureById() {

        // Given
        String adventureId = "WRLDID";

        AdventureDetails expectedResponse = GetAdventureResultFixture.privateMultiplayerAdventure().build();

        when(useCaseRunner.run(any(GetAdventureById.class))).thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri(String.format(ADVENTURE_ID_BASE_URL, adventureId))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(AdventureDetails.class)
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
    public void http201WhenCreateAdventure() {

        // Given
        CreateAdventureRequest request = CreateAdventureRequestFixture.sample();
        AdventureDetails expectedResponse = AdventureDetails.builder().id("WRLDID").build();

        when(adventureRequestMapper.toCommand(any(CreateAdventureRequest.class), anyString()))
                .thenReturn(mock(CreateAdventure.class));

        when(useCaseRunner.run(any(CreateAdventure.class))).thenReturn(expectedResponse);

        // Then
        webTestClient.post()
                .uri(ADVENTURE_BASE_URL)
                .bodyValue(request)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(AdventureDetails.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getId()).isEqualTo(expectedResponse.getId());
                });
    }

    @Test
    public void http200WhenUpdateAdventure() {

        // Given
        String adventureId = "WRLDID";
        UpdateAdventureRequest request = UpdateAdventureRequestFixture.sample();
        AdventureDetails expectedResponse = AdventureDetails.builder().lastUpdateDate(OffsetDateTime.now()).build();

        when(adventureRequestMapper.toCommand(any(UpdateAdventureRequest.class), anyString(), anyString()))
                .thenReturn(mock(UpdateAdventure.class));

        when(useCaseRunner.run(any(UpdateAdventure.class))).thenReturn(expectedResponse);

        // Then
        webTestClient.put()
                .uri(String.format(ADVENTURE_ID_BASE_URL, adventureId))
                .bodyValue(request)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(AdventureDetails.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getLastUpdateDate()).isEqualTo(expectedResponse.getLastUpdateDate());
                });
    }

    @Test
    public void http200WhenDeleteAdventure() {

        // Given
        String adventureId = "WRLDID";

        when(adventureRequestMapper.toCommand(anyString(), anyString()))
                .thenReturn(mock(DeleteAdventure.class));
        when(useCaseRunner.run(any(DeleteAdventure.class))).thenReturn(null);

        // Then
        webTestClient.delete()
                .uri(String.format(ADVENTURE_ID_BASE_URL, adventureId))
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    public void http201WhenAddFavoriteAdventure() {

        // Given
        FavoriteRequest request = new FavoriteRequest();
        request.setAssetId("1234");

        when(useCaseRunner.run(any(AddFavoriteAdventure.class))).thenReturn(null);

        // Then
        webTestClient.post()
                .uri(String.format(ADVENTURE_ID_BASE_URL, "favorite"))
                .bodyValue(request)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    public void http201WhenRemoveFavoriteAdventure() {

        // Given
        FavoriteRequest request = new FavoriteRequest();
        request.setAssetId("1234");

        when(useCaseRunner.run(any(RemoveFavoriteAdventure.class))).thenReturn(null);

        // Then
        webTestClient.delete()
                .uri(String.format(ADVENTURE_ID_BASE_URL, "favorite/1234"))
                .exchange()
                .expectStatus().is2xxSuccessful();
    }
}
