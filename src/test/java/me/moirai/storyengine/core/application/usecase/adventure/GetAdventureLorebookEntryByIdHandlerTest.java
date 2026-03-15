package me.moirai.storyengine.core.application.usecase.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryFixture;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@ExtendWith(MockitoExtension.class)
public class GetAdventureLorebookEntryByIdHandlerTest {

    @Mock
    private AdventureRepository repository;

    @InjectMocks
    private GetAdventureLorebookEntryByIdHandler handler;

    @Test
    public void errorWhenQueryIsNull() {

        // Given
        GetAdventureLorebookEntryById query = null;

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void errorWhenEntryIdIsNull() {

        // Given
        GetAdventureLorebookEntryById query = GetAdventureLorebookEntryById.builder().build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void errorWhenAdventureIdIsNull() {

        // Given
        GetAdventureLorebookEntryById query = GetAdventureLorebookEntryById.builder()
                .entryId("ENTRID")
                .build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void getAdventureLorebookEntryById() {

        // Given
        String adventureId = AdventureFixture.PUBLIC_ID;
        String requesterId = "4314324";

        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.sampleLorebookEntry().build();
        ReflectionTestUtils.setField(entry, "id", AdventureLorebookEntryFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(entry, "publicId", AdventureLorebookEntryFixture.PUBLIC_ID);

        Adventure baseAdventure = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        Adventure adventure = spy(baseAdventure);
        doReturn(entry).when(adventure).getLorebookEntryById(anyString());

        GetAdventureLorebookEntryById query = GetAdventureLorebookEntryById.builder()
                .entryId(AdventureLorebookEntryFixture.PUBLIC_ID)
                .adventureId(adventureId)
                .requesterId(requesterId)
                .build();

        when(repository.findByPublicId(anyString())).thenReturn(Optional.of(adventure));

        // When
        AdventureLorebookEntryDetails result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entry.getPublicId());
        assertThat(result.getName()).isEqualTo(entry.getName());
        assertThat(result.getRegex()).isEqualTo(entry.getRegex());
        assertThat(result.getDescription()).isEqualTo(entry.getDescription());
        assertThat(result.getPlayerId()).isEqualTo(entry.getPlayerId());
        assertThat(result.isPlayerCharacter()).isEqualTo(entry.isPlayerCharacter());
        assertThat(result.getCreationDate()).isNotNull();
        assertThat(result.getLastUpdateDate()).isNotNull();
    }
}
