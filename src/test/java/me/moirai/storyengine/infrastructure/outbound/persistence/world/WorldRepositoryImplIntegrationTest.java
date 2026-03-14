package me.moirai.storyengine.infrastructure.outbound.persistence.world;

import static me.moirai.storyengine.common.domain.Visibility.PUBLIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.set;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.transaction.Transactional;
import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.application.usecase.world.request.SearchWorlds;
import me.moirai.storyengine.core.application.usecase.world.result.GetWorldResult;
import me.moirai.storyengine.core.application.usecase.world.result.SearchWorldsResult;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.domain.world.WorldRepository;
import me.moirai.storyengine.infrastructure.outbound.persistence.FavoriteEntity;
import me.moirai.storyengine.infrastructure.outbound.persistence.FavoriteRepository;

public class WorldRepositoryImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WorldRepository repository;

    @Autowired
    private WorldJpaRepository jpaRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private WorldLorebookEntryJpaRepository lorebookEntryJpaRepository;

    @BeforeEach
    public void before() {
        lorebookEntryJpaRepository.deleteAllInBatch();
        jpaRepository.deleteAllInBatch();
    }

    @Test
    public void createWorld() {

        // Given
        World world = WorldFixture.privateWorld()
                .id(null)
                .build();

        // When
        World createdWorld = repository.save(world);

        // Then
        assertThat(createdWorld).isNotNull();

        assertThat(createdWorld.getCreationDate()).isNotNull();
        assertThat(createdWorld.getLastUpdateDate()).isNotNull();

        assertThat(createdWorld.getName()).isEqualTo(world.getName());
        assertThat(createdWorld.getVisibility()).isEqualTo(world.getVisibility());
        assertThat(createdWorld.getUsersAllowedToWrite()).hasSameElementsAs(world.getUsersAllowedToWrite());
        assertThat(createdWorld.getUsersAllowedToRead()).hasSameElementsAs(world.getUsersAllowedToRead());
    }

    @Test
    public void retrieveWorldById() {

        // Given
        World world = repository.save(WorldFixture.privateWorld()
                .id(null)
                .build());

        // When
        Optional<World> retrievedWorldOptional = repository.findById(world.getId());

        // Then
        assertThat(retrievedWorldOptional).isNotNull().isNotEmpty();

        World retrievedWorld = retrievedWorldOptional.get();
        assertThat(retrievedWorld.getId()).isEqualTo(world.getId());
    }

    @Test
    public void emptyResultWhenAssetDoesntExist() {

        // Given
        String worldId = "WRLDID";

        // When
        Optional<World> retrievedWorldOptional = repository.findById(worldId);

        // Then
        assertThat(retrievedWorldOptional).isNotNull().isEmpty();
    }

    @Test
    public void deleteWorld() {

        // Given
        World world = repository.save(WorldFixture.privateWorld()
                .id(null)
                .build());

        // When
        repository.deleteById(world.getId());

        // Then
        assertThat(repository.findById(world.getId())).isNotNull().isEmpty();
    }

    @Test
    public void updateWorld() {

        // Given
        World originalWorld = repository.save(WorldFixture.privateWorld()
                .id(null)
                .build());

        World worldToUbeUpdated = WorldFixture.privateWorld()
                .id(originalWorld.getId())
                .visibility(PUBLIC)
                .version(originalWorld.getVersion())
                .build();

        // When
        World updatedWorld = repository.save(worldToUbeUpdated);

        // Then
        assertThat(originalWorld.getVersion()).isZero();
        assertThat(updatedWorld.getVersion()).isOne();
    }

    @Test
    @Transactional
    public void deleteWorld_whenIsFavorite_thenDeleteFavorites() {

        // Given
        String userId = "1234";
        World originalWorld = repository.save(WorldFixture.privateWorld()
                .id(null)
                .build());

        FavoriteEntity favorite = favoriteRepository.save(FavoriteEntity.builder()
                .playerId(userId)
                .assetId(originalWorld.getId())
                .assetType("persona")
                .build());

        // When
        repository.deleteById(originalWorld.getId());

        // Then
        assertThat(repository.findById(originalWorld.getId())).isNotNull().isEmpty();
        assertThat(favoriteRepository.existsById(favorite.getId())).isFalse();
    }

    @Test
    public void returnAllWorldsWhenSearchingWithoutParameters() {

        // Given
        String ownerId = "586678721356875";

        World gpt4Omni = WorldFixture.privateWorld()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        World gpt4Mini = WorldFixture.privateWorld()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .usersAllowedToRead(set(ownerId))
                        .build())
                .build();

        World gpt354k = WorldFixture.privateWorld()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .build())
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchWorlds query = SearchWorlds.builder()
                .requesterId(ownerId)
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnOnlyWorldsWithReadAccessWhenSearchingWithoutParametersAsc() {

        // Given
        String ownerId = "586678721356875";

        World gpt4Omni = WorldFixture.privateWorld()
                .id(null)
                .build();

        World gpt4Mini = WorldFixture.privateWorld()
                .id(null)
                .build();

        World gpt354k = WorldFixture.privateWorld()
                .id(null)
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchWorlds query = SearchWorlds.builder()
                .requesterId(ownerId)
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(worlds.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void returnAllWorldsWhenSearchingWithoutParametersDesc() {

        // Given
        String ownerId = "586678721356875";

        World gpt4Omni = WorldFixture.privateWorld()
                .id(null)
                .build();

        World gpt4Mini = WorldFixture.privateWorld()
                .id(null)
                .build();

        World gpt354k = WorldFixture.privateWorld()
                .id(null)
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchWorlds query = SearchWorlds.builder()
                .direction("DESC")
                .requesterId(ownerId)
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(worlds.get(2).getName()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchWorldOrderByOwner() {

        // Given
        String ownerId = "586678721356875";

        World gpt4Omni = WorldFixture.privateWorld()
                .id(null)
                .name("Number 2")
                .build();

        World gpt4Mini = WorldFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .build();

        World gpt354k = WorldFixture.privateWorld()
                .id(null)
                .name("Number 3")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchWorlds query = SearchWorlds.builder()
                .page(1)
                .size(10)
                .requesterId(ownerId)
                .ownerId(ownerId)
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(worlds.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchWorldOrderByNameAsc() {

        // Given
        String ownerId = "586678721356875";

        World gpt4Omni = WorldFixture.privateWorld()
                .id(null)
                .name("Number 2")
                .build();

        World gpt4Mini = WorldFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .build();

        World gpt354k = WorldFixture.privateWorld()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchWorlds query = SearchWorlds.builder()
                .sortingField("name")
                .page(1)
                .size(10)
                .requesterId(ownerId)
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(worlds.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchWorldOrderByNameDesc() {

        // Given
        String ownerId = "586678721356875";

        World gpt4Omni = WorldFixture.privateWorld()
                .id(null)
                .name("Number 2")
                .build();

        World gpt4Mini = WorldFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .build();

        World gpt354k = WorldFixture.privateWorld()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchWorlds query = SearchWorlds.builder()
                .sortingField("name")
                .direction("DESC")
                .requesterId(ownerId)
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(worlds.get(2).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchWorldFilterByName() {

        // Given
        String ownerId = "586678721356875";

        World gpt4Omni = WorldFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .build();

        World gpt4Mini = WorldFixture.privateWorld()
                .id(null)
                .name("Number 2")
                .build();

        World gpt354k = WorldFixture.privateWorld()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchWorlds query = SearchWorlds.builder()
                .name("Number 2")
                .requesterId(ownerId)
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchWorld_whenReadAccess_andFilterByVisibility_thenReturnResults() {

        // Given
        String ownerId = "586678721356875";
        String visibilityToSearch = "public";
        World gpt4Omni = WorldFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .build();

        World gpt4Mini = WorldFixture.publicWorld()
                .id(null)
                .name("Number 2")
                .build();

        World gpt354k = WorldFixture.publicWorld()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchWorlds query = SearchWorlds.builder()
                .visibility(visibilityToSearch)
                .requesterId(ownerId)
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchWorld_whenWriteAccess_andFilterByVisibility_thenReturnResults() {

        // Given
        String ownerId = "586678721356875";
        String visibilityToSearch = "public";
        World gpt4Omni = WorldFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .build();

        World gpt4Mini = WorldFixture.publicWorld()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        World gpt354k = WorldFixture.publicWorld()
                .id(null)
                .name("Number 3")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchWorlds query = SearchWorlds.builder()
                .visibility(visibilityToSearch)
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnAllWorldsWhenSearchingWithoutParametersShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        World gpt4Omni = WorldFixture.privateWorld()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        World gpt4Mini = WorldFixture.privateWorld()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        World gpt354k = WorldFixture.privateWorld()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .build())
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchWorlds query = SearchWorlds.builder()
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnOnlyWorldsWithWriteAccessWhenSearchingWithoutParametersAsc() {

        // Given
        String ownerId = "586678721358363";

        World gpt4Omni = WorldFixture.privateWorld()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        World gpt4Mini = WorldFixture.privateWorld()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        World gpt354k = WorldFixture.privateWorld()
                .id(null)
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchWorlds query = SearchWorlds.builder()
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnAllWorldsWhenSearchingWithoutParametersDescShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        World gpt4Omni = WorldFixture.privateWorld()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        World gpt4Mini = WorldFixture.privateWorld()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        World gpt354k = WorldFixture.privateWorld()
                .id(null)
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchWorlds query = SearchWorlds.builder()
                .direction("DESC")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchWorldOrderByNameAscShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        World gpt4Omni = WorldFixture.privateWorld()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        World gpt4Mini = WorldFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        World gpt354k = WorldFixture.privateWorld()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchWorlds query = SearchWorlds.builder()
                .sortingField("name")
                .page(1)
                .size(10)
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchWorldOrderByNameDescShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        World gpt4Omni = WorldFixture.privateWorld()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        World gpt4Mini = WorldFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        World gpt354k = WorldFixture.privateWorld()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchWorlds query = SearchWorlds.builder()
                .sortingField("name")
                .direction("DESC")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchWorldFilterByNameShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        World gpt4Omni = WorldFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .build();

        World gpt4Mini = WorldFixture.privateWorld()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        World gpt354k = WorldFixture.privateWorld()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchWorlds query = SearchWorlds.builder()
                .name("Number 2")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void emptyResultWhenSearchingForWorldWithWriteAccessIfUserHasNoAccess() {

        // Given
        String ownerId = "586678721358363";

        World gpt4Omni = WorldFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        World gpt4Mini = WorldFixture.privateWorld()
                .id(null)
                .name("Number 2")
                .build();

        World gpt354k = WorldFixture.privateWorld()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchWorlds query = SearchWorlds.builder()
                .name("Number 2")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isEmpty();
    }

    @Test
    public void searcFavoritehWorlds_whenNoFilter_thenReturnAllResults() {

        // Given
        String ownerId = "586678721356875";
        World gpt4Omni = jpaRepository.save(WorldFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build());

        World gpt4Mini = jpaRepository.save(WorldFixture.publicWorld()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build());

        World gpt354k = jpaRepository.save(WorldFixture.publicWorld()
                .id(null)
                .name("Number 3")
                .build());

        FavoriteEntity favorite1 = FavoriteEntity.builder()
                .playerId(ownerId)
                .assetType("world")
                .assetId(gpt4Omni.getId())
                .build();

        FavoriteEntity favorite2 = FavoriteEntity.builder()
                .playerId(ownerId)
                .assetType("world")
                .assetId(gpt4Mini.getId())
                .build();

        FavoriteEntity favorite3 = FavoriteEntity.builder()
                .playerId(ownerId)
                .assetType("world")
                .assetId(gpt354k.getId())
                .build();

        favoriteRepository.saveAll(set(favorite1, favorite2, favorite3));

        SearchWorlds query = SearchWorlds.builder()
                .requesterId(ownerId)
                .favorites(true)
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);
    }

    @Test
    public void searcFavoritehWorlds_whenByVisibility_thenReturnResults() {

        // Given
        String ownerId = "586678721356875";
        World gpt4Omni = jpaRepository.save(WorldFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build());

        World gpt4Mini = jpaRepository.save(WorldFixture.publicWorld()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build());

        World gpt354k = jpaRepository.save(WorldFixture.publicWorld()
                .id(null)
                .name("Number 3")
                .build());

        FavoriteEntity favorite1 = FavoriteEntity.builder()
                .playerId(ownerId)
                .assetType("world")
                .assetId(gpt4Omni.getId())
                .build();

        FavoriteEntity favorite2 = FavoriteEntity.builder()
                .playerId(ownerId)
                .assetType("world")
                .assetId(gpt4Mini.getId())
                .build();

        FavoriteEntity favorite3 = FavoriteEntity.builder()
                .playerId(ownerId)
                .assetType("world")
                .assetId(gpt354k.getId())
                .build();

        favoriteRepository.saveAll(set(favorite1, favorite2, favorite3));

        SearchWorlds query = SearchWorlds.builder()
                .requesterId(ownerId)
                .visibility("public")
                .favorites(true)
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);
    }

    @Test
    public void searcFavoritehWorlds_whenFilterByName_thenReturnResults() {

        // Given
        String ownerId = "586678721356875";
        World gpt4Omni = jpaRepository.save(WorldFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build());

        World gpt4Mini = jpaRepository.save(WorldFixture.publicWorld()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build());

        World gpt354k = jpaRepository.save(WorldFixture.publicWorld()
                .id(null)
                .name("Number 3")
                .build());

        FavoriteEntity favorite1 = FavoriteEntity.builder()
                .playerId(ownerId)
                .assetType("world")
                .assetId(gpt4Omni.getId())
                .build();

        FavoriteEntity favorite2 = FavoriteEntity.builder()
                .playerId(ownerId)
                .assetType("world")
                .assetId(gpt4Mini.getId())
                .build();

        FavoriteEntity favorite3 = FavoriteEntity.builder()
                .playerId(ownerId)
                .assetType("world")
                .assetId(gpt354k.getId())
                .build();

        favoriteRepository.saveAll(set(favorite1, favorite2, favorite3));

        SearchWorlds query = SearchWorlds.builder()
                .requesterId(ownerId)
                .name("Number 3")
                .favorites(true)
                .build();

        // When
        SearchWorldsResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
    }
}