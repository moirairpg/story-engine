package me.moirai.discordbot.infrastructure.inbound.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.discordbot.core.application.usecase.world.result.CreateWorldLorebookEntryResult;
import me.moirai.discordbot.core.application.usecase.world.result.GetWorldLorebookEntryResult;
import me.moirai.discordbot.core.application.usecase.world.result.SearchWorldLorebookEntriesResult;
import me.moirai.discordbot.core.application.usecase.world.result.UpdateWorldLorebookEntryResult;
import me.moirai.discordbot.infrastructure.inbound.rest.response.CreateLorebookEntryResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.LorebookEntryResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.SearchLorebookEntriesResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.UpdateLorebookEntryResponse;

@ExtendWith(MockitoExtension.class)
public class WorldLorebookEntryResponseMapperTest {

    @InjectMocks
    private WorldLorebookEntryResponseMapper mapper;

    @Test
    public void searchResultToResponse() {

        // Given
        GetWorldLorebookEntryResult entryResult = GetWorldLorebookEntryResult.builder()
                .name("asdsad")
                .regex("dasdsad")
                .description("dasdasd")
                .build();

        SearchWorldLorebookEntriesResult result = SearchWorldLorebookEntriesResult.builder()
                .page(1)
                .totalPages(10)
                .items(20)
                .totalItems(100)
                .results(Collections.singletonList(entryResult))
                .build();

        // When
        SearchLorebookEntriesResponse response = mapper.toResponse(result);

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    public void getEntryResultToResponse() {

        // Given
        GetWorldLorebookEntryResult entryResult = GetWorldLorebookEntryResult.builder()
                .name("asdsad")
                .regex("dasdsad")
                .description("dasdasd")
                .build();

        // When
        LorebookEntryResponse response = mapper.toResponse(entryResult);

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    public void createEntryResultToResponse() {

        // Given
        CreateWorldLorebookEntryResult entryResult = CreateWorldLorebookEntryResult.build("ENTRID");

        // When
        CreateLorebookEntryResponse response = mapper.toResponse(entryResult);

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    public void updateEntryResultToResponse() {

        // Given
        UpdateWorldLorebookEntryResult entryResult = UpdateWorldLorebookEntryResult.build(OffsetDateTime.now());

        // When
        UpdateLorebookEntryResponse response = mapper.toResponse(entryResult);

        // Then
        assertThat(response).isNotNull();
    }
}
