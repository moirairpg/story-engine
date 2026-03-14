package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.world.CreateWorldResult;
import me.moirai.storyengine.core.port.inbound.world.GetWorldResult;
import me.moirai.storyengine.core.application.usecase.world.result.GetWorldResultFixture;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldsResult;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorldResult;
import me.moirai.storyengine.infrastructure.inbound.rest.response.CreateWorldResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.SearchWorldsResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.UpdateWorldResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.WorldResponse;

@ExtendWith(MockitoExtension.class)
public class WorldResponseMapperTest {

    @InjectMocks
    private WorldResponseMapper mapper;

    @Test
    public void searchWorldResultToResponse() {

        // Given
        List<GetWorldResult> results = Lists.list(GetWorldResultFixture.publicWorld().build(),
                GetWorldResultFixture.privateWorld().build());

        SearchWorldsResult result = SearchWorldsResult.builder()
                .page(1)
                .totalPages(2)
                .totalItems(20)
                .items(10)
                .results(results)
                .build();

        // When
        SearchWorldsResponse response = mapper.toResponse(result);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPage()).isEqualTo(result.getPage());
        assertThat(response.getTotalPages()).isEqualTo(result.getTotalPages());
        assertThat(response.getResultsInPage()).isEqualTo(result.getItems());
        assertThat(response.getTotalResults()).isEqualTo(result.getTotalItems());
    }

    @Test
    public void getWorldResultToResponse() {

        // Given
        GetWorldResult result = GetWorldResultFixture.privateWorld().build();

        // When
        WorldResponse response = mapper.toResponse(result);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(result.getId());
        assertThat(response.getName()).isEqualTo(result.getName());
        assertThat(response.getDescription()).isEqualTo(result.getDescription());
        assertThat(response.getAdventureStart()).isEqualTo(result.getAdventureStart());
        assertThat(response.getVisibility()).isEqualTo(result.getVisibility());
        assertThat(response.getOwnerId()).isEqualTo(result.getOwnerId());
        assertThat(response.getCreationDate()).isEqualTo(result.getCreationDate());
        assertThat(response.getLastUpdateDate()).isEqualTo(result.getLastUpdateDate());
    }

    @Test
    public void createWorldResultToResponse() {

        // Given
        CreateWorldResult result = CreateWorldResult.build("WRLDID");

        // When
        CreateWorldResponse response = mapper.toResponse(result);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(result.getId());
    }

    @Test
    public void updateWorldResultToResponse() {

        // Given
        UpdateWorldResult result = UpdateWorldResult.build(OffsetDateTime.now());

        // When
        UpdateWorldResponse response = mapper.toResponse(result);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getLastUpdateDate()).isEqualTo(result.getLastUpdatedDateTime());
    }
}
