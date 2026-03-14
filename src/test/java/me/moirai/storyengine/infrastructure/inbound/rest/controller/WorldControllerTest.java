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
import me.moirai.storyengine.core.port.inbound.world.AddFavoriteWorld;
import me.moirai.storyengine.core.port.inbound.world.CreateWorld;
import me.moirai.storyengine.core.port.inbound.world.CreateWorldResult;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorld;
import me.moirai.storyengine.core.port.inbound.world.GetWorldById;
import me.moirai.storyengine.core.port.inbound.world.GetWorldResult;
import me.moirai.storyengine.core.port.inbound.world.RemoveFavoriteWorld;
import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldsResult;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorld;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorldResult;
import me.moirai.storyengine.core.application.usecase.world.result.GetWorldResultFixture;
import me.moirai.storyengine.infrastructure.inbound.rest.mapper.WorldRequestMapper;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateWorldRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateWorldRequestFixture;
import me.moirai.storyengine.infrastructure.inbound.rest.request.FavoriteRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateWorldRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateWorldRequestFixture;
import me.moirai.storyengine.infrastructure.security.authentication.config.AuthenticationSecurityConfig;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = {
        WorldController.class
}, excludeAutoConfiguration = {
        ReactiveSecurityAutoConfiguration.class,
        AuthenticationSecurityConfig.class
})
public class WorldControllerTest extends AbstractRestWebTest {

    private static final String WORLD_BASE_URL = "/world";
    private static final String WORLD_ID_BASE_URL = "/world/%s";

    @MockBean
    protected WorldRequestMapper worldRequestMapper;

    @Test
    public void http200WhenSearchWorlds() {

        // Given
        List<GetWorldResult> results = Lists.list(GetWorldResultFixture.publicWorld().build(),
                GetWorldResultFixture.privateWorld().build());

        SearchWorldsResult expectedResponse = SearchWorldsResult.builder()
                .page(1)
                .totalPages(2)
                .totalItems(20)
                .items(10)
                .results(results)
                .build();

        when(useCaseRunner.run(any(SearchWorlds.class))).thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri(String.format(WORLD_BASE_URL))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(SearchWorldsResult.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getTotalPages()).isEqualTo(2);
                    assertThat(response.getTotalItems()).isEqualTo(20);
                    assertThat(response.getItems()).isEqualTo(10);
                    assertThat(response.getResults()).hasSameSizeAs(results);
                });
    }

    @Test
    public void http200WhenSearchWorldsWithParameters() {

        // Given
        List<GetWorldResult> results = Lists.list(GetWorldResultFixture.publicWorld().build(),
                GetWorldResultFixture.privateWorld().build());

        SearchWorldsResult expectedResponse = SearchWorldsResult.builder()
                .page(1)
                .totalPages(2)
                .totalItems(20)
                .items(10)
                .results(results)
                .build();

        when(useCaseRunner.run(any(SearchWorlds.class))).thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri(uri -> uri.path(String.format(WORLD_BASE_URL))
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
                .expectBody(SearchWorldsResult.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getTotalPages()).isEqualTo(2);
                    assertThat(response.getTotalItems()).isEqualTo(20);
                    assertThat(response.getItems()).isEqualTo(10);
                    assertThat(response.getResults()).hasSameSizeAs(results);
                });
    }

    @Test
    public void http200WhenGetWorldById() {

        // Given
        String worldId = "WRLDID";

        GetWorldResult expectedResponse = GetWorldResultFixture.publicWorld().build();

        when(useCaseRunner.run(any(GetWorldById.class))).thenReturn(expectedResponse);

        // Then
        webTestClient.get()
                .uri(String.format(WORLD_ID_BASE_URL, worldId))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(GetWorldResult.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getId()).isEqualTo(expectedResponse.getId());
                    assertThat(response.getName()).isEqualTo(expectedResponse.getName());
                    assertThat(response.getDescription()).isEqualTo(expectedResponse.getDescription());
                    assertThat(response.getAdventureStart()).isEqualTo(expectedResponse.getAdventureStart());
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
    public void http201WhenCreateWorld() {

        // Given
        CreateWorldRequest request = CreateWorldRequestFixture.createPrivateWorld();
        CreateWorldResult expectedResponse = CreateWorldResult.build("WRLDID");

        when(worldRequestMapper.toCommand(any(CreateWorldRequest.class), anyString()))
                .thenReturn(mock(CreateWorld.class));
        when(useCaseRunner.run(any(CreateWorld.class))).thenReturn(Mono.just(expectedResponse));

        // Then
        webTestClient.post()
                .uri(WORLD_BASE_URL)
                .bodyValue(request)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(CreateWorldResult.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getId()).isEqualTo(expectedResponse.getId());
                });
    }

    @Test
    public void http200WhenUpdateWorld() {

        // Given
        String worldId = "WRLDID";
        UpdateWorldRequest request = UpdateWorldRequestFixture.createPrivateWorld();
        UpdateWorldResult expectedResponse = UpdateWorldResult.build(OffsetDateTime.now());

        when(worldRequestMapper.toCommand(any(UpdateWorldRequest.class), anyString(), anyString()))
                .thenReturn(mock(UpdateWorld.class));
        when(useCaseRunner.run(any(UpdateWorld.class))).thenReturn(Mono.just(expectedResponse));

        // Then
        webTestClient.put()
                .uri(String.format(WORLD_ID_BASE_URL, worldId))
                .bodyValue(request)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(UpdateWorldResult.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getLastUpdatedDateTime()).isEqualTo(expectedResponse.getLastUpdatedDateTime());
                });
    }

    @Test
    public void http200WhenDeleteWorld() {

        // Given
        String worldId = "WRLDID";

        when(useCaseRunner.run(any(DeleteWorld.class))).thenReturn(null);

        // Then
        webTestClient.delete()
                .uri(String.format(WORLD_ID_BASE_URL, worldId))
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    public void http201WhenAddFavoriteWorld() {

        // Given
        FavoriteRequest request = new FavoriteRequest();
        request.setAssetId("1234");

        when(useCaseRunner.run(any(AddFavoriteWorld.class))).thenReturn(null);

        // Then
        webTestClient.post()
                .uri(String.format(WORLD_ID_BASE_URL, "favorite"))
                .bodyValue(request)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    public void http201WhenRemoveFavoriteWorld() {

        // Given
        FavoriteRequest request = new FavoriteRequest();
        request.setAssetId("1234");

        when(useCaseRunner.run(any(RemoveFavoriteWorld.class))).thenReturn(null);

        // Then
        webTestClient.delete()
                .uri(String.format(WORLD_ID_BASE_URL, "favorite/1234"))
                .exchange()
                .expectStatus().is2xxSuccessful();
    }
}
