package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.application.usecase.adventure.request.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.application.usecase.adventure.result.GetAdventureLorebookEntryResult;
import me.moirai.storyengine.core.application.usecase.adventure.result.SearchAdventureLorebookEntriesResult;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureRepository;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryRepository;

public class AdventureLorebookEntryRepositoryImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AdventureLorebookEntryRepository repository;

    @Autowired
    private AdventureLorebookEntryJpaRepository jpaRepository;

    @Autowired
    private AdventureJpaRepository adventureJpaRepository;

    @Autowired
    private AdventureRepository adventureRepository;

    @BeforeEach
    public void before() {
        jpaRepository.deleteAllInBatch();
        adventureJpaRepository.deleteAllInBatch();
    }

    @Test
    public void createAdventureLorebookEntry() {

        // Given
        Adventure adventure = adventureJpaRepository.save(AdventureFixture.publicMultiplayerAdventure().build());
        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .adventureId(adventure.getId())
                .build();

        // When
        AdventureLorebookEntry createdAdventureLorebookEntry = repository.save(entry);

        // Then
        assertThat(createdAdventureLorebookEntry).isNotNull();

        assertThat(createdAdventureLorebookEntry.getCreationDate()).isNotNull();
        assertThat(createdAdventureLorebookEntry.getLastUpdateDate()).isNotNull();

        assertThat(createdAdventureLorebookEntry.getName()).isEqualTo(entry.getName());
    }

    @Test
    public void retrieveAdventureLorebookEntryById() {

        // Given
        Adventure adventure = adventureJpaRepository.save(AdventureFixture.publicMultiplayerAdventure().build());
        AdventureLorebookEntry entry = repository.save(AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .adventureId(adventure.getId())
                .build());

        // When
        Optional<AdventureLorebookEntry> retrievedAdventureLorebookEntryOptional = repository.findById(entry.getId());

        // Then
        assertThat(retrievedAdventureLorebookEntryOptional).isNotNull().isNotEmpty();

        AdventureLorebookEntry retrievedAdventureLorebookEntry = retrievedAdventureLorebookEntryOptional.get();
        assertThat(retrievedAdventureLorebookEntry.getId()).isEqualTo(entry.getId());
    }

    @Test
    public void retrieveAdventureLorebookEntryByPlayerId() {

        // Given
        String playerId = "4234234";
        Adventure adventure = adventureRepository.save(AdventureFixture.publicMultiplayerAdventure().build());
        AdventureLorebookEntry entry = repository.save(AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .playerId(playerId)
                .adventureId(adventure.getId())
                .build());

        // When
        Optional<AdventureLorebookEntry> retrievedAdventureLorebookEntryOptional = repository
                .findByPlayerId(playerId, adventure.getId());

        // Then
        assertThat(retrievedAdventureLorebookEntryOptional).isNotNull().isNotEmpty();

        AdventureLorebookEntry retrievedAdventureLorebookEntry = retrievedAdventureLorebookEntryOptional.get();
        assertThat(retrievedAdventureLorebookEntry.getId()).isEqualTo(entry.getId());
    }

    @Test
    public void emptyResultWhenAssetDoesntExist() {

        // Given
        String entryId = "WRLDID";

        // When
        Optional<AdventureLorebookEntry> retrievedAdventureLorebookEntryOptional = repository.findById(entryId);

        // Then
        assertThat(retrievedAdventureLorebookEntryOptional).isNotNull().isEmpty();
    }

    @Test
    public void deleteAdventureLorebookEntry() {

        // Given
        Adventure adventure = adventureRepository.save(AdventureFixture.publicMultiplayerAdventure().build());
        AdventureLorebookEntry entry = repository.save(AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .adventureId(adventure.getId())
                .build());

        // When
        repository.deleteById(entry.getId());

        // Then
        assertThat(repository.findById(entry.getId())).isNotNull().isEmpty();
    }

    @Test
    public void searchEntries_whenNoSearchParameters_thenReturnAllEntriesThatExist() {

        // Given
        Adventure adventure = adventureRepository.save(AdventureFixture.publicMultiplayerAdventure().build());
        AdventureLorebookEntry gpt4Omni = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry gpt4Mini = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry gpt354k = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .adventureId(adventure.getId())
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchAdventureLorebookEntries query = SearchAdventureLorebookEntries.builder()
                .adventureId(adventure.getId())
                .build();

        // When
        SearchAdventureLorebookEntriesResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetAdventureLorebookEntryResult> entries = result.getResults();
        assertThat(entries.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(entries.get(1).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(entries.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchEntries_whenNoParameters_andOrderingAscending_thenReturnAllEntriesThatExistAscending() {

        // Given
        Adventure adventure = adventureRepository.save(AdventureFixture.publicMultiplayerAdventure().build());
        AdventureLorebookEntry gpt4Omni = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry gpt4Mini = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry gpt354k = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .adventureId(adventure.getId())
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchAdventureLorebookEntries query = SearchAdventureLorebookEntries.builder()
                .adventureId(adventure.getId())
                .direction("ASC")
                .build();

        // When
        SearchAdventureLorebookEntriesResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetAdventureLorebookEntryResult> entries = result.getResults();
        assertThat(entries.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(entries.get(1).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(entries.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchEntries_whenNoParameters_andOrderingDescending_thenReturnAllEntriesThatExistDescending() {

        // Given
        Adventure adventure = adventureRepository.save(AdventureFixture.publicMultiplayerAdventure().build());
        AdventureLorebookEntry gpt4Omni = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry gpt4Mini = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry gpt354k = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .adventureId(adventure.getId())
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchAdventureLorebookEntries query = SearchAdventureLorebookEntries.builder()
                .adventureId(adventure.getId())
                .direction("DESC")
                .build();

        // When
        SearchAdventureLorebookEntriesResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetAdventureLorebookEntryResult> entries = result.getResults();
        assertThat(entries.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(entries.get(1).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(entries.get(2).getName()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchEntries_whenNoParameters_andOrderByName_andDirectionAscending_thenReturnAllEntriesThatExistInOrderByNameAscending() {

        // Given
        Adventure adventure = adventureRepository.save(AdventureFixture.publicMultiplayerAdventure().build());
        AdventureLorebookEntry gpt4Omni = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .name("Number 2")
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry gpt4Mini = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .name("Number 1")
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry gpt354k = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .name("Number 3")
                .adventureId(adventure.getId())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventureLorebookEntries query = SearchAdventureLorebookEntries.builder()
                .adventureId(adventure.getId())
                .sortingField("name")
                .page(1)
                .size(10)
                .build();

        // When
        SearchAdventureLorebookEntriesResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetAdventureLorebookEntryResult> entries = result.getResults();
        assertThat(entries.get(0).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(entries.get(1).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(entries.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchEntries_whenNoParameters_andOrderByName_andDirectionDescending_thenReturnAllEntriesThatExistInOrderByNameDescending() {

        // Given
        Adventure adventure = adventureRepository.save(AdventureFixture.publicMultiplayerAdventure().build());
        AdventureLorebookEntry gpt4Omni = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .name("Number 2")
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry gpt4Mini = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .name("Number 1")
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry gpt354k = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .name("Number 3")
                .adventureId(adventure.getId())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventureLorebookEntries query = SearchAdventureLorebookEntries.builder()
                .adventureId(adventure.getId())
                .sortingField("name")
                .direction("DESC")
                .build();

        // When
        SearchAdventureLorebookEntriesResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetAdventureLorebookEntryResult> entries = result.getResults();
        assertThat(entries.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(entries.get(1).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(entries.get(2).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchEntries_whenFilteredByName_thenReturnOnlyEntriesWithThatName() {

        // Given
        Adventure adventure = adventureRepository.save(AdventureFixture.publicMultiplayerAdventure().build());
        AdventureLorebookEntry gpt4Omni = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .name("Number 1")
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry gpt4Mini = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .name("Number 2")
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry gpt354k = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .name("Number 3")
                .adventureId(adventure.getId())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventureLorebookEntries query = SearchAdventureLorebookEntries.builder()
                .adventureId(adventure.getId())
                .name("Number 2")
                .build();

        // When
        SearchAdventureLorebookEntriesResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<GetAdventureLorebookEntryResult> entries = result.getResults();
        assertThat(entries.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void findAllEntriesByRegex_whenValidRegex_thenReturnMatchingEntries() {

        // Then
        Adventure adventure = adventureRepository.save(AdventureFixture.privateMultiplayerAdventure().build());
        AdventureLorebookEntry gpt4Omni = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .name("John")
                .regex("[Jj]ohn")
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry gpt4Mini = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .name("Immune")
                .regex("[Ii]mmun(e|ity)")
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry gpt354k = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .name("Archmage")
                .regex("[Aa]rch(|-|\s)[Mm]age")
                .adventureId(adventure.getId())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4Omni, gpt4Mini, gpt354k));

        String archmageLowerCase = "archmage";
        String archmageDash = "Arch-mage";
        String archmageSpace = "Arch Mage";
        String immunityLowerCase = "immunity";
        String triggerAll = "John, the Arch-Mage, is immune to disease";

        // When
        List<AdventureLorebookEntry> archmageLowerCaseResult = repository.findAllByRegex(archmageLowerCase,
                adventure.getId());
        List<AdventureLorebookEntry> archmageDashResult = repository.findAllByRegex(archmageDash, adventure.getId());
        List<AdventureLorebookEntry> archmageSpaceResult = repository.findAllByRegex(archmageSpace, adventure.getId());
        List<AdventureLorebookEntry> immunityLowerCaseResult = repository.findAllByRegex(immunityLowerCase,
                adventure.getId());
        List<AdventureLorebookEntry> allEntries = repository.findAllByRegex(triggerAll, adventure.getId());

        // Then
        assertThat(archmageLowerCaseResult).isNotNull().isNotEmpty().hasSize(1);
        assertThat(archmageDashResult).isNotNull().isNotEmpty().hasSize(1);
        assertThat(archmageSpaceResult).isNotNull().isNotEmpty().hasSize(1);
        assertThat(immunityLowerCaseResult).isNotNull().isNotEmpty().hasSize(1);
        assertThat(allEntries).isNotNull().isNotEmpty().hasSize(3);
    }

    @Test
    public void updateAdventure() {

        // Given
        Adventure adventure = adventureRepository.save(AdventureFixture.privateMultiplayerAdventure().build());
        AdventureLorebookEntry originalEntry = repository.save(AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .version(0)
                .adventureId(adventure.getId())
                .build());

        AdventureLorebookEntry entryToBeUpdated = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(originalEntry.getId())
                .name("new name")
                .version(originalEntry.getVersion())
                .adventureId(adventure.getId())
                .build();

        // When
        AdventureLorebookEntry updatedAdventureLorebookEntry = repository.save(entryToBeUpdated);

        // Then
        assertThat(originalEntry.getVersion()).isZero();
        assertThat(updatedAdventureLorebookEntry.getVersion()).isOne();
    }
}
