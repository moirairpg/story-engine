package me.moirai.storyengine.core.domain.adventure;

import static me.moirai.storyengine.core.domain.adventure.Moderation.DISABLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.port.outbound.TextModerationResult;
import me.moirai.storyengine.core.application.model.result.TextModerationResultFixture;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntry;
import me.moirai.storyengine.core.application.usecase.adventure.request.CreateAdventureLorebookEntryFixture;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntry;
import me.moirai.storyengine.core.application.usecase.adventure.request.UpdateAdventureLorebookEntryFixture;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.port.TextModerationPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class AdventureServiceImplTest {

    @Mock
    private TextModerationPort moderationPort;

    @Mock
    private AdventureLorebookEntryRepository lorebookEntryRepository;

    @Mock
    private AdventureRepository repository;

    @InjectMocks
    private AdventureServiceImpl service;

    @Test
    public void createLorebookEntry_whenAdventureNotFound_thenThrowException() {

        // Given
        String expectedError = "Adventure to be updated was not found";
        CreateAdventureLorebookEntry command = CreateAdventureLorebookEntryFixture.sampleLorebookEntry().build();

        when(repository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.createLorebookEntry(command))
                .hasMessage(expectedError)
                .isInstanceOf(AssetNotFoundException.class);
    }

    @Test
    public void createLorebookEntry_whenNoPermissionToModifyWorld_thenThrowException() {

        // Given
        String expectedError = "User does not have permission to modify this adventure";
        CreateAdventureLorebookEntry command = CreateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .requesterId("1234")
                .build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(null)
                        .ownerId("4234234234234234")
                        .build())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));

        // Then
        assertThatThrownBy(() -> service.createLorebookEntry(command))
                .hasMessage(expectedError)
                .isInstanceOf(AssetAccessDeniedException.class);
    }

    @Test
    public void createLorebookEntry_whenInappropriateContentFoundInName_andModerationIsAbsolute_thenThrowException() {

        String requesterId = "123123123";
        String expectedError = "Adventure flagged by moderation";
        TextModerationResult flaggedResult = TextModerationResultFixture.withFlags().build();
        CreateAdventureLorebookEntry command = CreateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .requesterId(requesterId)
                .build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(moderationPort.moderate(anyString())).thenReturn(Mono.just(flaggedResult));

        // Then
        StepVerifier.create(service.createLorebookEntry(command))
                .verifyErrorSatisfies(error -> assertThat(error).isNotNull()
                        .isInstanceOf(ModerationException.class)
                        .hasMessage(expectedError));
    }

    @Test
    public void createLorebookEntry_whenInappropriateContentFoundInDescription_andModerationIsAbsolute_thenThrowException() {

        String requesterId = "123123123";
        String expectedError = "Adventure flagged by moderation";
        TextModerationResult flaggedResult = TextModerationResultFixture.withFlags().build();
        TextModerationResult safeResult = TextModerationResultFixture.withoutFlags().build();
        CreateAdventureLorebookEntry command = CreateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .requesterId(requesterId)
                .build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(safeResult))
                .thenReturn(Mono.just(flaggedResult));

        // Then
        StepVerifier.create(service.createLorebookEntry(command))
                .verifyErrorSatisfies(error -> assertThat(error).isNotNull()
                        .isInstanceOf(ModerationException.class)
                        .hasMessage(expectedError));
    }

    @Test
    public void createLorebookEntry_whenInappropriateContentFoundInName_andModerationIsPermissive_thenThrowException() {

        String requesterId = "123123123";
        String expectedError = "Adventure flagged by moderation";
        TextModerationResult flaggedResult = TextModerationResultFixture.withFlags().build();
        CreateAdventureLorebookEntry command = CreateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .requesterId(requesterId)
                .build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .moderation(Moderation.PERMISSIVE)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(moderationPort.moderate(anyString())).thenReturn(Mono.just(flaggedResult));

        // Then
        StepVerifier.create(service.createLorebookEntry(command))
                .verifyErrorSatisfies(error -> assertThat(error).isNotNull()
                        .isInstanceOf(ModerationException.class)
                        .hasMessage(expectedError));
    }

    @Test
    public void createLorebookEntry_whenInappropriateContentFoundInDescription_andModerationIsPermissive_thenThrowException() {

        String requesterId = "123123123";
        String expectedError = "Adventure flagged by moderation";
        TextModerationResult flaggedResult = TextModerationResultFixture.withFlags().build();
        TextModerationResult safeResult = TextModerationResultFixture.withoutFlags().build();
        CreateAdventureLorebookEntry command = CreateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .requesterId(requesterId)
                .build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
        .moderation(Moderation.PERMISSIVE)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(safeResult))
                .thenReturn(Mono.just(flaggedResult));

        // Then
        StepVerifier.create(service.createLorebookEntry(command))
                .verifyErrorSatisfies(error -> assertThat(error).isNotNull()
                        .isInstanceOf(ModerationException.class)
                        .hasMessage(expectedError));
    }

    @Test
    public void createLorebookEntry_whenInappropriateContentFound_andModerationIsDisabled_thenDontFlagTopics() {

        String requesterId = "123123123";
        TextModerationResult moderationResult = TextModerationResultFixture.withFlags().build();
        CreateAdventureLorebookEntry command = CreateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .requesterId(requesterId)
                .build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .moderation(DISABLED)
                .build();

        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.sampleLorebookEntry().build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(lorebookEntryRepository.save(any())).thenReturn(entry);
        when(moderationPort.moderate(anyString())).thenReturn(Mono.just(moderationResult));

        // Then
        StepVerifier.create(service.createLorebookEntry(command))
                .assertNext(entryCreated -> {
                    assertThat(entryCreated).isNotNull();
                    assertThat(entryCreated.getId()).isSameAs(entry.getId());
                })
                .verifyComplete();
    }

    @Test
    public void createLorebookEntry_whenValidData_thenEntryIsCreated() {

        String requesterId = "123123123";
        TextModerationResult moderationResult = TextModerationResultFixture.withoutFlags().build();
        CreateAdventureLorebookEntry command = CreateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .requesterId(requesterId)
                .build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.sampleLorebookEntry().build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(lorebookEntryRepository.save(any())).thenReturn(entry);
        when(moderationPort.moderate(anyString())).thenReturn(Mono.just(moderationResult));

        // Then
        StepVerifier.create(service.createLorebookEntry(command))
                .assertNext(entryCreated -> {
                    assertThat(entryCreated).isNotNull();
                    assertThat(entryCreated.getId()).isSameAs(entry.getId());
                })
                .verifyComplete();
    }

    @Test
    public void findById_whenAdventureNotFound_thenThrowException() {

        // Given
        String expectedError = "Adventure to be viewed was not found";
        GetAdventureLorebookEntryById command = GetAdventureLorebookEntryById.builder()
                .adventureId("1234")
                .entryId("1234")
                .requesterId("1234")
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.findLorebookEntryById(command))
                .hasMessage(expectedError)
                .isInstanceOf(AssetNotFoundException.class);
    }

    @Test
    public void findById_whenInsufficientWritingPermissionInAdventure_thenThrowException() {

        // Given
        String expectedError = "User does not have permission to view this adventure";
        GetAdventureLorebookEntryById command = GetAdventureLorebookEntryById.builder()
                .adventureId("1234")
                .entryId("1234")
                .requesterId("1234")
                .build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(null)
                        .ownerId("89790796786")
                        .build())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));

        // Then
        assertThatThrownBy(() -> service.findLorebookEntryById(command))
                .hasMessage(expectedError)
                .isInstanceOf(AssetAccessDeniedException.class);
    }

    @Test
    public void findById_whenLorebookEntryNotFound_thenThrowException() {

        // Given
        String expectedError = "Lorebook entry to be viewed was not found";
        GetAdventureLorebookEntryById command = GetAdventureLorebookEntryById.builder()
                .adventureId("1234")
                .entryId("1234")
                .requesterId("1234")
                .build();

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("1234")
                        .build())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.findLorebookEntryById(command))
                .hasMessage(expectedError)
                .isInstanceOf(AssetNotFoundException.class);
    }

    @Test
    public void findById_whenLorebookEntryFound_andUserHasPermissions_thenReturnQueriedEntry() {

        // Given
        GetAdventureLorebookEntryById command = GetAdventureLorebookEntryById.builder()
                .adventureId("1234")
                .entryId("1234")
                .requesterId("1234")
                .build();

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("1234")
                        .build())
                .build();

        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .adventureId(adventure.getId())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.of(entry));

        // When
        AdventureLorebookEntry retrievedEntry = service.findLorebookEntryById(command);

        // Then
        assertThat(retrievedEntry).isNotNull();
        assertThat(retrievedEntry.getId()).isEqualTo(entry.getId());
        assertThat(retrievedEntry.getName()).isEqualTo(entry.getName());
        assertThat(retrievedEntry.getRegex()).isEqualTo(entry.getRegex());
        assertThat(retrievedEntry.getDescription()).isEqualTo(entry.getDescription());
        assertThat(retrievedEntry.getCreatorId()).isEqualTo(entry.getCreatorId());
        assertThat(retrievedEntry.getAdventureId()).isEqualTo(entry.getAdventureId());
        assertThat(retrievedEntry.getVersion()).isEqualTo(entry.getVersion());
        assertThat(retrievedEntry.getLastUpdateDate()).isEqualTo(entry.getLastUpdateDate());
        assertThat(retrievedEntry.getCreationDate()).isEqualTo(entry.getCreationDate());
    }

    @Test
    public void findByPlayerId_whenAdventureNotFound_thenThrowException() {

        // Given
        String expectedError = "Adventure to be viewed was not found";
        String playerId = "123123";
        String adventureId = "6456456";

        when(repository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.findLorebookEntryByPlayerDiscordId(playerId, adventureId))
                .hasMessage(expectedError)
                .isInstanceOf(AssetNotFoundException.class);
    }

    @Test
    public void findByPlayerId_whenLorebookEntryNotFound_thenThrowException() {

        // Given
        String playerId = "123123";
        String adventureId = "6456456";
        String expectedError = "Lorebook entry to be viewed was not found";

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("1234")
                        .build())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(lorebookEntryRepository.findByPlayerId(anyString(), anyString())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.findLorebookEntryByPlayerDiscordId(playerId, adventureId))
                .hasMessage(expectedError)
                .isInstanceOf(AssetNotFoundException.class);
    }

    @Test
    public void findByPlayerId_whenLorebookEntryFound_thenReturnQueriedEntry() {

        // Given
        String playerId = "123123";
        String adventureId = "6456456";

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("1234")
                        .build())
                .build();

        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .adventureId(adventure.getId())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(lorebookEntryRepository.findByPlayerId(anyString(), anyString())).thenReturn(Optional.of(entry));

        // When
        AdventureLorebookEntry retrievedEntry = service.findLorebookEntryByPlayerDiscordId(
                playerId, adventureId);

        // Then
        assertThat(retrievedEntry).isNotNull();
        assertThat(retrievedEntry.getId()).isEqualTo(entry.getId());
        assertThat(retrievedEntry.getName()).isEqualTo(entry.getName());
        assertThat(retrievedEntry.getRegex()).isEqualTo(entry.getRegex());
        assertThat(retrievedEntry.getDescription()).isEqualTo(entry.getDescription());
        assertThat(retrievedEntry.getCreatorId()).isEqualTo(entry.getCreatorId());
        assertThat(retrievedEntry.getAdventureId()).isEqualTo(entry.getAdventureId());
        assertThat(retrievedEntry.getVersion()).isEqualTo(entry.getVersion());
        assertThat(retrievedEntry.getLastUpdateDate()).isEqualTo(entry.getLastUpdateDate());
        assertThat(retrievedEntry.getCreationDate()).isEqualTo(entry.getCreationDate());
    }

    @Test
    public void findByRegex_whenAdventureNotFound_thenThrowException() {

        // Given
        String expectedError = "Adventure to be viewed was not found";
        String valueToMatch = "SomeName";
        String adventureId = "6456456";

        when(repository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.findAllLorebookEntriesByRegex(valueToMatch, adventureId))
                .hasMessage(expectedError)
                .isInstanceOf(AssetNotFoundException.class);
    }

    @Test
    public void findByRegex_whenLorebookEntriesFound_thenReturnQueriedEntries() {

        // Given
        String valueToMatch = "SomeName";
        String adventureId = "6456456";

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("1234")
                        .build())
                .build();

        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .adventureId(adventure.getId())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(lorebookEntryRepository.findAllByRegex(anyString(), anyString())).thenReturn(list(entry));

        // When
        List<AdventureLorebookEntry> result = service.findAllLorebookEntriesByRegex(valueToMatch, adventureId);

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result.get(0).getId()).isEqualTo(entry.getId());
        assertThat(result.get(0).getName()).isEqualTo(entry.getName());
        assertThat(result.get(0).getRegex()).isEqualTo(entry.getRegex());
        assertThat(result.get(0).getDescription()).isEqualTo(entry.getDescription());
        assertThat(result.get(0).getCreatorId()).isEqualTo(entry.getCreatorId());
        assertThat(result.get(0).getAdventureId()).isEqualTo(entry.getAdventureId());
        assertThat(result.get(0).getVersion()).isEqualTo(entry.getVersion());
        assertThat(result.get(0).getLastUpdateDate()).isEqualTo(entry.getLastUpdateDate());
        assertThat(result.get(0).getCreationDate()).isEqualTo(entry.getCreationDate());
    }

    @Test
    public void deleteEntry_whenAdventureNotFound_thenThrowException() {

        // Given
        String expectedError = "Adventure to be updated was not found";
        DeleteAdventureLorebookEntry command = DeleteAdventureLorebookEntry.builder()
                .adventureId("1234")
                .lorebookEntryId("1234")
                .requesterId("1234")
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.deleteLorebookEntry(command))
                .hasMessage(expectedError)
                .isInstanceOf(AssetNotFoundException.class);
    }

    @Test
    public void deleteEntry_whenInsufficientPermissionsToDelete_thenThrowException() {

        // Given
        String expectedError = "User does not have permission to modify this adventure";
        DeleteAdventureLorebookEntry command = DeleteAdventureLorebookEntry.builder()
                .adventureId("1234")
                .lorebookEntryId("1234")
                .requesterId("1234")
                .build();

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure().build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));

        // Then
        assertThatThrownBy(() -> service.deleteLorebookEntry(command))
                .hasMessage(expectedError)
                .isInstanceOf(AssetAccessDeniedException.class);
    }

    @Test
    public void deleteEntry_whenEntryNotFound_thenThrowException() {

        // Given
        String expectedError = "Lorebook entry to be deleted was not found";
        DeleteAdventureLorebookEntry command = DeleteAdventureLorebookEntry.builder()
                .adventureId("1234")
                .lorebookEntryId("1234")
                .requesterId("1234")
                .build();

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("1234")
                        .build())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.deleteLorebookEntry(command))
                .hasMessage(expectedError)
                .isInstanceOf(AssetNotFoundException.class);
    }

    @Test
    public void deleteEntry_whenEntryFound_andPermissionsValid_thenDeleteEntry() {

        // Given
        DeleteAdventureLorebookEntry command = DeleteAdventureLorebookEntry.builder()
                .adventureId("1234")
                .lorebookEntryId("1234")
                .requesterId("1234")
                .build();

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("1234")
                        .build())
                .build();

        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .adventureId(adventure.getId())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.of(entry));

        // When
        service.deleteLorebookEntry(command);

        // Then
        verify(lorebookEntryRepository, times(1)).deleteById(anyString());
    }

    @Test
    public void updateEntry_whenAdventureNotFound_thenThrowException() {

        // Given
        String expectedError = "Adventure to be updated was not found";
        UpdateAdventureLorebookEntry command = UpdateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .adventureId("1234")
                .id("1234")
                .requesterId("1234")
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.updateLorebookEntry(command))
                .hasMessage(expectedError)
                .isInstanceOf(AssetNotFoundException.class);
    }

    @Test
    public void updateEntry_whenInsufficientPermissions_thenThrowException() {

        // Given
        String expectedError = "User does not have permission to modify this adventure";
        UpdateAdventureLorebookEntry command = UpdateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .adventureId("1234")
                .id("1234")
                .requesterId("1234")
                .build();

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure().build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));

        // Then
        assertThatThrownBy(() -> service.updateLorebookEntry(command))
                .hasMessage(expectedError)
                .isInstanceOf(AssetAccessDeniedException.class);
    }

    @Test
    public void updateEntry_whenInappropriateContentFoundInName_thenThrowException() {

        String requesterId = "123123123";
        String expectedError = "Adventure flagged by moderation";
        TextModerationResult flaggedResult = TextModerationResultFixture.withFlags().build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        UpdateAdventureLorebookEntry command = UpdateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .adventureId(adventure.getId())
                .id("1234")
                .requesterId(requesterId)
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(moderationPort.moderate(anyString())).thenReturn(Mono.just(flaggedResult));

        // Then
        StepVerifier.create(service.updateLorebookEntry(command))
                .verifyErrorSatisfies(error -> assertThat(error).isNotNull()
                        .isInstanceOf(ModerationException.class)
                        .hasMessage(expectedError));
    }

    @Test
    public void updateEntry_whenInappropriateContentFoundInDescription_thenThrowException() {

        String requesterId = "123123123";
        String expectedError = "Adventure flagged by moderation";
        TextModerationResult flaggedResult = TextModerationResultFixture.withFlags().build();
        TextModerationResult safeResult = TextModerationResultFixture.withoutFlags().build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        UpdateAdventureLorebookEntry command = UpdateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .adventureId(adventure.getId())
                .id("1234")
                .requesterId(requesterId)
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(safeResult))
                .thenReturn(Mono.just(flaggedResult));

        // Then
        StepVerifier.create(service.updateLorebookEntry(command))
                .verifyErrorSatisfies(error -> assertThat(error).isNotNull()
                        .isInstanceOf(ModerationException.class)
                        .hasMessage(expectedError));
    }

    @Test
    public void updateEntry_whenEntryNotFound_thenThrowException() {

        // Given
        String requesterId = "123123123";
        String expectedError = "Lorebook entry to be updated was not found";
        TextModerationResult safeResult = TextModerationResultFixture.withoutFlags().build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        UpdateAdventureLorebookEntry command = UpdateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .adventureId(adventure.getId())
                .id("1234")
                .requesterId(requesterId)
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(moderationPort.moderate(anyString())).thenReturn(Mono.just(safeResult));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        StepVerifier.create(service.updateLorebookEntry(command))
                .verifyErrorSatisfies(error -> assertThat(error).isNotNull()
                        .isInstanceOf(AssetNotFoundException.class)
                        .hasMessage(expectedError));
    }

    @Test
    public void updateEntry_whenValidData_thenEntryIsUpdated() {

        String requesterId = "123123123";
        TextModerationResult moderationResult = TextModerationResultFixture.withoutFlags().build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        UpdateAdventureLorebookEntry command = UpdateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .adventureId(adventure.getId())
                .id("1234")
                .requesterId(requesterId)
                .build();

        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.sampleLorebookEntry().build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(moderationPort.moderate(anyString())).thenReturn(Mono.just(moderationResult));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.of(entry));
        when(lorebookEntryRepository.save(any())).thenReturn(entry);

        // Then
        StepVerifier.create(service.updateLorebookEntry(command))
                .assertNext(entryCreated -> {
                    assertThat(entryCreated).isNotNull();
                    assertThat(entryCreated.getId()).isSameAs(entry.getId());
                })
                .verifyComplete();
    }

    @Test
    public void updateEntry_whenValidData_andIsToMakePlayableCharacter_thenEntryIsUpdated() {

        String requesterId = "123123123";
        TextModerationResult moderationResult = TextModerationResultFixture.withoutFlags().build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        UpdateAdventureLorebookEntry command = UpdateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .adventureId(adventure.getId())
                .id("1234")
                .requesterId(requesterId)
                .playerId(requesterId)
                .isPlayerCharacter(true)
                .build();

        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.sampleLorebookEntry().build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(moderationPort.moderate(anyString())).thenReturn(Mono.just(moderationResult));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.of(entry));
        when(lorebookEntryRepository.save(any())).thenReturn(entry);

        // Then
        StepVerifier.create(service.updateLorebookEntry(command))
                .assertNext(entryCreated -> {
                    assertThat(entryCreated).isNotNull();
                    assertThat(entryCreated.getId()).isSameAs(entry.getId());
                })
                .verifyComplete();
    }

    @Test
    public void updateEntry_whenNothingToChange_thenEntryIsUpdated() {

        String requesterId = "123123123";
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.samplePlayerCharacterLorebookEntry().build();
        UpdateAdventureLorebookEntry command = UpdateAdventureLorebookEntry.builder()
                .adventureId(adventure.getId())
                .id(entry.getId())
                .requesterId(requesterId)
                .isPlayerCharacter(false)
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.of(entry));
        when(lorebookEntryRepository.save(any())).thenReturn(entry);

        // Then
        StepVerifier.create(service.updateLorebookEntry(command))
                .assertNext(entryCreated -> {
                    assertThat(entryCreated).isNotNull();
                    assertThat(entryCreated.getId()).isSameAs(entry.getId());
                })
                .verifyComplete();
    }
}
