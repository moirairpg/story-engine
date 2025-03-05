package me.moirai.discordbot.infrastructure.inbound.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.discordbot.core.application.usecase.adventure.result.CreateAdventureResult;
import me.moirai.discordbot.core.application.usecase.adventure.result.GetAdventureResult;
import me.moirai.discordbot.core.application.usecase.adventure.result.GetAdventureResultFixture;
import me.moirai.discordbot.core.application.usecase.adventure.result.SearchAdventuresResult;
import me.moirai.discordbot.core.application.usecase.adventure.result.UpdateAdventureResult;
import me.moirai.discordbot.infrastructure.inbound.rest.response.AdventureResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.CreateAdventureResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.SearchAdventuresResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.UpdateAdventureResponse;

@ExtendWith(MockitoExtension.class)
public class AdventureResponseMapperTest {

    @InjectMocks
    private AdventureResponseMapper mapper;

    @Test
    public void searchAdventureResultToResponse() {

        // Given
        List<GetAdventureResult> results = Lists.list(GetAdventureResultFixture.privateMultiplayerAdventure().build(),
                GetAdventureResultFixture.privateMultiplayerAdventure().build());

        SearchAdventuresResult result = SearchAdventuresResult.builder()
                .page(1)
                .totalPages(2)
                .totalItems(20)
                .items(10)
                .results(results)
                .build();

        // When
        SearchAdventuresResponse response = mapper.toResponse(result);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPage()).isEqualTo(result.getPage());
        assertThat(response.getTotalPages()).isEqualTo(result.getTotalPages());
        assertThat(response.getResultsInPage()).isEqualTo(result.getItems());
        assertThat(response.getTotalResults()).isEqualTo(result.getTotalItems());
    }

    @Test
    public void getAdventureResultToResponse() {

        // Given
        GetAdventureResult result = GetAdventureResultFixture.privateMultiplayerAdventure().build();

        // When
        AdventureResponse response = mapper.toResponse(result);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(result.getId());
        assertThat(response.getName()).isEqualTo(result.getName());
        assertThat(response.getVisibility()).isEqualTo(result.getVisibility());
        assertThat(response.getOwnerDiscordId()).isEqualTo(result.getOwnerDiscordId());
        assertThat(response.getCreationDate()).isEqualTo(result.getCreationDate());
        assertThat(response.getLastUpdateDate()).isEqualTo(result.getLastUpdateDate());
    }

    @Test
    public void createAdventureResultToResponse() {

        // Given
        CreateAdventureResult result = CreateAdventureResult.build("WRLDID");

        // When
        CreateAdventureResponse response = mapper.toResponse(result);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(result.getId());
    }

    @Test
    public void updateAdventureResultToResponse() {

        // Given
        UpdateAdventureResult result = UpdateAdventureResult.build(OffsetDateTime.now());

        // When
        UpdateAdventureResponse response = mapper.toResponse(result);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getLastUpdateDate()).isEqualTo(result.getLastUpdatedDateTime());
    }
}
