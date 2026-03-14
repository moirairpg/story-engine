package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.adventure.result.CreateAdventureLorebookEntryResult;
import me.moirai.storyengine.core.application.usecase.adventure.result.GetAdventureLorebookEntryResult;
import me.moirai.storyengine.core.application.usecase.adventure.result.SearchAdventureLorebookEntriesResult;
import me.moirai.storyengine.core.application.usecase.adventure.result.UpdateAdventureLorebookEntryResult;
import me.moirai.storyengine.infrastructure.inbound.rest.response.CreateLorebookEntryResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.LorebookEntryResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.SearchLorebookEntriesResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.UpdateLorebookEntryResponse;

@ExtendWith(MockitoExtension.class)
public class AdventureLorebookEntryResponseMapperTest {

    @InjectMocks
    private AdventureLorebookEntryResponseMapper mapper;

    @Test
    public void searchResultToResponse() {

        // Given
        GetAdventureLorebookEntryResult entryResult = GetAdventureLorebookEntryResult.builder()
                .name("asdsad")
                .regex("dasdsad")
                .description("dasdasd")
                .build();

        SearchAdventureLorebookEntriesResult result = SearchAdventureLorebookEntriesResult.builder()
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
        GetAdventureLorebookEntryResult entryResult = GetAdventureLorebookEntryResult.builder()
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
        CreateAdventureLorebookEntryResult entryResult = CreateAdventureLorebookEntryResult.build("ENTRID");

        // When
        CreateLorebookEntryResponse response = mapper.toResponse(entryResult);

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    public void updateEntryResultToResponse() {

        // Given
        UpdateAdventureLorebookEntryResult entryResult = UpdateAdventureLorebookEntryResult.build(OffsetDateTime.now());

        // When
        UpdateLorebookEntryResponse response = mapper.toResponse(entryResult);

        // Then
        assertThat(response).isNotNull();
    }
}
