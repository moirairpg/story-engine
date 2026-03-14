package me.moirai.storyengine.infrastructure.outbound.adapter.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;

import java.util.List;
import java.util.Optional;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.application.usecase.world.request.SearchWorldLorebookEntries;
import me.moirai.storyengine.core.application.usecase.world.result.GetWorldLorebookEntryResult;
import me.moirai.storyengine.core.application.usecase.world.result.SearchWorldLorebookEntriesResult;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldRepository;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntryFixture;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntryRepository;

public class WorldLorebookEntryRepositoryImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WorldLorebookEntryRepository repository;

    @Autowired
    private WorldLorebookEntryJpaRepository jpaRepository;

    @Autowired
    private WorldJpaRepository worldJpaRepository;

    @Autowired
    private WorldRepository worldRepository;

    @BeforeEach
    public void before() {
        jpaRepository.deleteAllInBatch();
        worldJpaRepository.deleteAllInBatch();
    }

    @Test
    public void createWorldLorebookEntry() {

        // Given
        World world = worldJpaRepository.save(WorldFixture.publicWorld().build());
        WorldLorebookEntry entry = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .build();

        // When
        WorldLorebookEntry createdWorldLorebookEntry = repository.save(entry);

        // Then
        assertThat(createdWorldLorebookEntry).isNotNull();

        assertThat(createdWorldLorebookEntry.getCreationDate()).isNotNull();
        assertThat(createdWorldLorebookEntry.getLastUpdateDate()).isNotNull();

        assertThat(createdWorldLorebookEntry.getName()).isEqualTo(entry.getName());
    }

    @Test
    public void retrieveWorldLorebookEntryById() {

        // Given
        World world = worldJpaRepository.save(WorldFixture.publicWorld().build());
        WorldLorebookEntry entry = repository.save(WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .build());

        // When
        Optional<WorldLorebookEntry> retrievedWorldLorebookEntryOptional = repository.findById(entry.getId());

        // Then
        assertThat(retrievedWorldLorebookEntryOptional).isNotNull().isNotEmpty();

        WorldLorebookEntry retrievedWorldLorebookEntry = retrievedWorldLorebookEntryOptional.get();
        assertThat(retrievedWorldLorebookEntry.getId()).isEqualTo(entry.getId());
    }

    @Test
    public void emptyResultWhenAssetDoesntExist() {

        // Given
        String entryId = "WRLDID";

        // When
        Optional<WorldLorebookEntry> retrievedWorldLorebookEntryOptional = repository.findById(entryId);

        // Then
        assertThat(retrievedWorldLorebookEntryOptional).isNotNull().isEmpty();
    }

    @Test
    public void deleteWorldLorebookEntry() {

        // Given
        World world = worldJpaRepository.save(WorldFixture.publicWorld().build());
        WorldLorebookEntry entry = repository.save(WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .build());

        // When
        repository.deleteById(entry.getId());

        // Then
        assertThat(repository.findById(entry.getId())).isNotNull().isEmpty();
    }

    @Test
    public void returnAllWorldLorebookEntriesWhenSearchingWithoutParameters() {

        // Given
        World world = worldJpaRepository.save(WorldFixture.publicWorld().build());
        WorldLorebookEntry gpt4Omni = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .build();

        WorldLorebookEntry gpt4Mini = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .build();

        WorldLorebookEntry gpt354k = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchWorldLorebookEntries query = SearchWorldLorebookEntries.builder()
                .worldId(world.getId())
                .build();

        // When
        SearchWorldLorebookEntriesResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetWorldLorebookEntryResult> entries = result.getResults();
        assertThat(entries.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(entries.get(1).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(entries.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void returnOnlyWorldLorebookEntriesWithReadAccessWhenSearchingWithoutParametersAsc() {

        // Given
        World world = worldJpaRepository.save(WorldFixture.publicWorld().build());
        WorldLorebookEntry gpt4Omni = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .build();

        WorldLorebookEntry gpt4Mini = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .build();

        WorldLorebookEntry gpt354k = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchWorldLorebookEntries query = SearchWorldLorebookEntries.builder()
                .worldId(world.getId())
                .build();

        // When
        SearchWorldLorebookEntriesResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetWorldLorebookEntryResult> entries = result.getResults();
        assertThat(entries.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(entries.get(1).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(entries.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void returnAllWorldLorebookEntriesWhenSearchingWithoutParametersDesc() {

        // Given
        World world = worldJpaRepository.save(WorldFixture.publicWorld().build());
        WorldLorebookEntry gpt4Omni = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .build();

        WorldLorebookEntry gpt4Mini = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .build();

        WorldLorebookEntry gpt354k = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchWorldLorebookEntries query = SearchWorldLorebookEntries.builder()
                .worldId(world.getId())
                .direction("DESC")
                .build();

        // When
        SearchWorldLorebookEntriesResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetWorldLorebookEntryResult> entries = result.getResults();
        assertThat(entries.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(entries.get(1).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(entries.get(2).getName()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchWorldLorebookEntryOrderByNameAsc() {

        // Given
        World world = worldJpaRepository.save(WorldFixture.publicWorld().build());
        WorldLorebookEntry gpt4Omni = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .name("Number 2")
                .build();

        WorldLorebookEntry gpt4Mini = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .name("Number 1")
                .build();

        WorldLorebookEntry gpt354k = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .name("Number 3")
                .build();

        jpaRepository.saveAll(Lists.list(gpt4Omni, gpt4Mini, gpt354k));

        SearchWorldLorebookEntries query = SearchWorldLorebookEntries.builder()
                .worldId(world.getId())
                .sortingField("name")
                .page(1)
                .size(10)
                .build();

        // When
        SearchWorldLorebookEntriesResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetWorldLorebookEntryResult> entries = result.getResults();
        assertThat(entries.get(0).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(entries.get(1).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(entries.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchWorldLorebookEntryOrderByNameDesc() {

        // Given
        World world = worldJpaRepository.save(WorldFixture.publicWorld().build());
        WorldLorebookEntry gpt4Omni = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .name("Number 2")
                .build();

        WorldLorebookEntry gpt4Mini = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .name("Number 1")
                .build();

        WorldLorebookEntry gpt354k = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .name("Number 3")
                .build();

        jpaRepository.saveAll(Lists.list(gpt4Omni, gpt4Mini, gpt354k));

        SearchWorldLorebookEntries query = SearchWorldLorebookEntries.builder()
                .worldId(world.getId())
                .sortingField("name")
                .direction("DESC")
                .build();

        // When
        SearchWorldLorebookEntriesResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetWorldLorebookEntryResult> entries = result.getResults();
        assertThat(entries.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(entries.get(1).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(entries.get(2).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchWorldLorebookEntryFilterByName() {

        // Given
        World world = worldJpaRepository.save(WorldFixture.publicWorld().build());
        WorldLorebookEntry gpt4Omni = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .name("Number 1")
                .build();

        WorldLorebookEntry gpt4Mini = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .name("Number 2")
                .build();

        WorldLorebookEntry gpt354k = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .name("Number 3")
                .build();

        jpaRepository.saveAll(Lists.list(gpt4Omni, gpt4Mini, gpt354k));

        SearchWorldLorebookEntries query = SearchWorldLorebookEntries.builder()
                .worldId(world.getId())
                .name("Number 2")
                .build();

        // When
        SearchWorldLorebookEntriesResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<GetWorldLorebookEntryResult> entries = result.getResults();
        assertThat(entries.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void findAllEntriesByRegex_whenValidRegex_thenReturnMatchingEntries() {

        // Then
        World world = worldRepository.save(WorldFixture.privateWorld().build());
        WorldLorebookEntry gpt4Omni = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .name("John")
                .regex("[Jj]ohn")
                .worldId(world.getId())
                .build();

        WorldLorebookEntry gpt4Mini = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .name("Immune")
                .regex("[Ii]mmun(e|ity)")
                .worldId(world.getId())
                .build();

        WorldLorebookEntry gpt354k = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .name("Archmage")
                .regex("[Aa]rch(|-|\s)[Mm]age")
                .worldId(world.getId())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4Omni, gpt4Mini, gpt354k));

        String archmageLowerCase = "archmage";
        String archmageDash = "Arch-mage";
        String archmageSpace = "Arch Mage";
        String immunityLowerCase = "immunity";
        String triggerAll = "John, the Arch-Mage, is immune to disease";

        // When
        List<WorldLorebookEntry> archmageLowerCaseResult = repository.findAllByRegex(archmageLowerCase, world.getId());
        List<WorldLorebookEntry> archmageDashResult = repository.findAllByRegex(archmageDash, world.getId());
        List<WorldLorebookEntry> archmageSpaceResult = repository.findAllByRegex(archmageSpace, world.getId());
        List<WorldLorebookEntry> immunityLowerCaseResult = repository.findAllByRegex(immunityLowerCase, world.getId());
        List<WorldLorebookEntry> allEntries = repository.findAllByRegex(triggerAll, world.getId());

        // Then
        assertThat(archmageLowerCaseResult).isNotNull().isNotEmpty().hasSize(1);
        assertThat(archmageDashResult).isNotNull().isNotEmpty().hasSize(1);
        assertThat(archmageSpaceResult).isNotNull().isNotEmpty().hasSize(1);
        assertThat(immunityLowerCaseResult).isNotNull().isNotEmpty().hasSize(1);
        assertThat(allEntries).isNotNull().isNotEmpty().hasSize(3);
    }

    @Test
    public void updateWorld() {

        // Given
        World world = worldJpaRepository.save(WorldFixture.publicWorld().build());
        WorldLorebookEntry originalEntry = repository.save(WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .worldId(world.getId())
                .build());

        WorldLorebookEntry entryToBeUpdated = WorldLorebookEntryFixture.sampleLorebookEntry()
                .id(originalEntry.getId())
                .worldId(world.getId())
                .name("new name")
                .version(originalEntry.getVersion())
                .build();

        // When
        WorldLorebookEntry updatedWorldLorebookEntry = repository.save(entryToBeUpdated);

        // Then
        assertThat(originalEntry.getVersion()).isZero();
        assertThat(updatedWorldLorebookEntry.getVersion()).isOne();
    }

    @Test
    public void findAllByWorldId() {

        // Given
        World world = worldRepository.save(WorldFixture.publicWorld().build());
        jpaRepository.saveAll(list(
                WorldLorebookEntryFixture.sampleLorebookEntry().id(null).worldId(world.getId()).build(),
                WorldLorebookEntryFixture.sampleLorebookEntry().id(null).worldId(world.getId()).build(),
                WorldLorebookEntryFixture.sampleLorebookEntry().id(null).worldId(world.getId()).build()));

        // When
        List<WorldLorebookEntry> result = repository.findAllByWorldId(world.getId());

        // Then
        assertThat(result).isNotNull().isNotEmpty().hasSize(3);
    }
}
