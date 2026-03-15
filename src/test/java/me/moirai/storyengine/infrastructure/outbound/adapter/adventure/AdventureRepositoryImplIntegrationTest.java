package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import static java.util.Collections.singleton;
import static me.moirai.storyengine.common.domain.Visibility.PRIVATE;
import static me.moirai.storyengine.common.domain.Visibility.PUBLIC;
import static me.moirai.storyengine.core.domain.adventure.ArtificialIntelligenceModel.GPT35_TURBO;
import static me.moirai.storyengine.core.domain.adventure.GameMode.AUTHOR;
import static me.moirai.storyengine.core.domain.adventure.GameMode.CHAT;
import static me.moirai.storyengine.core.domain.adventure.GameMode.RPG;
import static me.moirai.storyengine.core.domain.adventure.Moderation.DISABLED;
import static me.moirai.storyengine.core.domain.adventure.Moderation.PERMISSIVE;
import static me.moirai.storyengine.core.domain.adventure.Moderation.STRICT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.common.domain.Visibility;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventuresResult;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.GameMode;
import me.moirai.storyengine.core.domain.adventure.ModelConfigurationFixture;
import me.moirai.storyengine.core.domain.adventure.Moderation;


public class AdventureRepositoryImplIntegrationTest extends AbstractIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AdventureRepository repository;

    @Autowired
    private AdventureJpaRepository jpaRepository;

    @Autowired
    private AdventureLorebookEntryJpaRepository lorebookEntryJpaRepository;

    @BeforeEach
    public void before() {
        lorebookEntryJpaRepository.deleteAllInBatch();
        jpaRepository.deleteAllInBatch();
    }

    @Test
    public void createAdventure() {

        // Given
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .id(null)
                .build();

        // When
        Adventure createdAdventure = repository.save(adventure);

        // Then
        assertThat(createdAdventure).isNotNull();

        assertThat(createdAdventure.getCreationDate()).isNotNull();
        assertThat(createdAdventure.getLastUpdateDate()).isNotNull();

        assertThat(createdAdventure.getModelConfiguration().getAiModel().toString())
                .isEqualTo((adventure.getModelConfiguration().getAiModel().toString()));

        assertThat(createdAdventure.getModelConfiguration().getFrequencyPenalty())
                .isEqualTo((adventure.getModelConfiguration().getFrequencyPenalty()));

        assertThat(createdAdventure.getModelConfiguration().getPresencePenalty())
                .isEqualTo((adventure.getModelConfiguration().getPresencePenalty()));

        assertThat(createdAdventure.getModelConfiguration().getTemperature())
                .isEqualTo((adventure.getModelConfiguration().getTemperature()));

        assertThat(createdAdventure.getModelConfiguration().getLogitBias())
                .isEqualTo((adventure.getModelConfiguration().getLogitBias()));

        assertThat(createdAdventure.getModelConfiguration().getMaxTokenLimit())
                .isEqualTo((adventure.getModelConfiguration().getMaxTokenLimit()));

        assertThat(createdAdventure.getModelConfiguration().getStopSequences())
                .isEqualTo((adventure.getModelConfiguration().getStopSequences()));

    }

    @Test
    public void emptyResultWhenAssetDoesntExist() {

        // Given
        String adventureId = "WRLDID";

        // When
        Optional<Adventure> retrievedAdventureOptional = repository.findById(adventureId);

        // Then
        assertThat(retrievedAdventureOptional).isNotNull().isEmpty();
    }

    @Test
    public void deleteAdventure() {

        // Given
        Adventure adventure = repository.save(AdventureFixture.privateMultiplayerAdventure()
                .id(null)
                .build());

        // When
        repository.deleteById(adventure.getId());

        // Then
        assertThat(repository.findById(adventure.getId())).isNotNull().isEmpty();
    }

    @Test
    public void updateAdventure() {

        // Given
        Adventure originalAdventure = repository.save(AdventureFixture.privateMultiplayerAdventure()
                .id(null)
                .version(0)
                .build());

        Adventure worldToUbeUpdated = AdventureFixture.privateMultiplayerAdventure()
                .id(originalAdventure.getId())
                .visibility(Visibility.PUBLIC)
                .version(originalAdventure.getVersion())
                .build();

        // When
        Adventure updatedAdventure = repository.save(worldToUbeUpdated);

        // Then
        assertThat(originalAdventure.getVersion()).isZero();
        assertThat(updatedAdventure.getVersion()).isOne();
    }

    @Test
    @Transactional
    public void updateRememberAdventure() {

        // Given
        String remember = "new value";
        String channelId = "123123123";

        repository.save(AdventureFixture.privateMultiplayerAdventure()
                .id(null)
                .version(0)
                .channelId(channelId)
                .build());

        entityManager.flush();
        entityManager.clear();

        // When
        repository.updateRememberByChannelId(remember, channelId);

        // Then
        Adventure updatedAdventure = repository.findByChannelId(channelId).get();
        assertThat(updatedAdventure.getContextAttributes().getRemember()).isEqualTo(remember);
    }

    @Test
    @Transactional
    public void updateAuthorsNoteAdventure() {

        // Given
        String authorsNote = "new value";
        String channelId = "123123123";

        repository.save(AdventureFixture.privateMultiplayerAdventure()
                .id(null)
                .version(0)
                .channelId(channelId)
                .build());

        entityManager.flush();
        entityManager.clear();

        // When
        repository.updateAuthorsNoteByChannelId(authorsNote, channelId);

        // Then
        Adventure updatedAdventure = repository.findByChannelId(channelId).get();
        assertThat(updatedAdventure.getContextAttributes().getAuthorsNote()).isEqualTo(authorsNote);
    }

    @Test
    @Transactional
    public void updateNudgeAdventure() {

        // Given
        String nudge = "new value";
        String channelId = "123123123";

        repository.save(AdventureFixture.privateMultiplayerAdventure()
                .id(null)
                .version(0)
                .channelId(channelId)
                .build());

        entityManager.flush();
        entityManager.clear();

        // When
        repository.updateNudgeByChannelId(nudge, channelId);

        // Then
        Adventure updatedAdventure = repository.findByChannelId(channelId).get();
        assertThat(updatedAdventure.getContextAttributes().getNudge()).isEqualTo(nudge);
    }

    @Test
    @Transactional
    public void updateBumpAdventure() {

        // Given
        int bumpFrequency = 35;
        String bump = "new value";
        String channelId = "123123123";

        repository.save(AdventureFixture.privateMultiplayerAdventure()
                .id(null)
                .version(0)
                .channelId(channelId)
                .build());

        entityManager.flush();
        entityManager.clear();

        // When
        repository.updateBumpByChannelId(bump, bumpFrequency, channelId);

        // Then
        Adventure updatedAdventure = repository.findByChannelId(channelId).get();
        assertThat(updatedAdventure.getContextAttributes().getBump()).isEqualTo(bump);
        assertThat(updatedAdventure.getContextAttributes().getBumpFrequency()).isEqualTo(bumpFrequency);
    }

    @Test
    public void retrieveAdventureById() {

        // Given
        String adventureId = "234234";
        Adventure adventure = jpaRepository.save(AdventureFixture.publicMultiplayerAdventure()
                .id(adventureId)
                .channelId(adventureId)
                .build());

        // When
        Optional<Adventure> retrievedAdventureOptional = repository.findById(adventureId);

        // Then
        assertThat(retrievedAdventureOptional).isNotNull().isNotEmpty();

        Adventure retrievedAdventure = retrievedAdventureOptional.get();
        assertThat(retrievedAdventure.getId()).isEqualTo(adventure.getId());
        assertThat(retrievedAdventure.getAdventureStart()).isEqualTo(adventure.getAdventureStart());
        assertThat(retrievedAdventure.getDescription()).isEqualTo(adventure.getDescription());
        assertThat(retrievedAdventure.getChannelId()).isEqualTo(adventure.getChannelId());
        assertThat(retrievedAdventure.getGameMode()).isEqualTo(adventure.getGameMode());
        assertThat(retrievedAdventure.getName()).isEqualTo(adventure.getName());
        assertThat(retrievedAdventure.getOwnerId()).isEqualTo(adventure.getOwnerId());
        assertThat(retrievedAdventure.getPersonaId()).isEqualTo(adventure.getPersonaId());
        assertThat(retrievedAdventure.getVisibility()).isEqualTo(adventure.getVisibility());
        assertThat(retrievedAdventure.getModeration()).isEqualTo(adventure.getModeration());
        assertThat(retrievedAdventure.getWorldId()).isEqualTo(adventure.getWorldId());

        assertThat(retrievedAdventure.getModelConfiguration().getAiModel())
                .isEqualTo(adventure.getModelConfiguration().getAiModel());
        assertThat(retrievedAdventure.getModelConfiguration().getFrequencyPenalty())
                .isEqualTo(adventure.getModelConfiguration().getFrequencyPenalty());
        assertThat(retrievedAdventure.getModelConfiguration().getLogitBias())
                .isEqualTo(adventure.getModelConfiguration().getLogitBias());
        assertThat(retrievedAdventure.getModelConfiguration().getMaxTokenLimit())
                .isEqualTo(adventure.getModelConfiguration().getMaxTokenLimit());
        assertThat(retrievedAdventure.getModelConfiguration().getPresencePenalty())
                .isEqualTo(adventure.getModelConfiguration().getPresencePenalty());
        assertThat(retrievedAdventure.getModelConfiguration().getStopSequences())
                .isEqualTo(adventure.getModelConfiguration().getStopSequences());
        assertThat(retrievedAdventure.getModelConfiguration().getTemperature())
                .isEqualTo(adventure.getModelConfiguration().getTemperature());

        assertThat(retrievedAdventure.getUsersAllowedToRead()).hasSameElementsAs(adventure.getUsersAllowedToRead());
        assertThat(retrievedAdventure.getUsersAllowedToWrite()).hasSameElementsAs(adventure.getUsersAllowedToWrite());
    }

    @Test
    public void retrieveAdventureByChannelId() {

        // Given
        String channelId = "234234";
        Adventure adventure = jpaRepository.save(AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .channelId(channelId)
                .build());

        // When
        Optional<Adventure> retrievedAdventureOptional = repository.findByChannelId(channelId);

        // Then
        assertThat(retrievedAdventureOptional).isNotNull().isNotEmpty();

        Adventure retrievedAdventure = retrievedAdventureOptional.get();
        assertThat(retrievedAdventure.getId()).isEqualTo(adventure.getId());
        assertThat(retrievedAdventure.getAdventureStart()).isEqualTo(adventure.getAdventureStart());
        assertThat(retrievedAdventure.getDescription()).isEqualTo(adventure.getDescription());
        assertThat(retrievedAdventure.getChannelId()).isEqualTo(adventure.getChannelId());
        assertThat(retrievedAdventure.getGameMode()).isEqualTo(adventure.getGameMode());
        assertThat(retrievedAdventure.getName()).isEqualTo(adventure.getName());
        assertThat(retrievedAdventure.getOwnerId()).isEqualTo(adventure.getOwnerId());
        assertThat(retrievedAdventure.getPersonaId()).isEqualTo(adventure.getPersonaId());
        assertThat(retrievedAdventure.getVisibility()).isEqualTo(adventure.getVisibility());
        assertThat(retrievedAdventure.getModeration()).isEqualTo(adventure.getModeration());
        assertThat(retrievedAdventure.getWorldId()).isEqualTo(adventure.getWorldId());

        assertThat(retrievedAdventure.getModelConfiguration().getAiModel())
                .isEqualTo(adventure.getModelConfiguration().getAiModel());
        assertThat(retrievedAdventure.getModelConfiguration().getFrequencyPenalty())
                .isEqualTo(adventure.getModelConfiguration().getFrequencyPenalty());
        assertThat(retrievedAdventure.getModelConfiguration().getLogitBias())
                .isEqualTo(adventure.getModelConfiguration().getLogitBias());
        assertThat(retrievedAdventure.getModelConfiguration().getMaxTokenLimit())
                .isEqualTo(adventure.getModelConfiguration().getMaxTokenLimit());
        assertThat(retrievedAdventure.getModelConfiguration().getPresencePenalty())
                .isEqualTo(adventure.getModelConfiguration().getPresencePenalty());
        assertThat(retrievedAdventure.getModelConfiguration().getStopSequences())
                .isEqualTo(adventure.getModelConfiguration().getStopSequences());
        assertThat(retrievedAdventure.getModelConfiguration().getTemperature())
                .isEqualTo(adventure.getModelConfiguration().getTemperature());

        assertThat(retrievedAdventure.getUsersAllowedToRead()).hasSameElementsAs(adventure.getUsersAllowedToRead());
        assertThat(retrievedAdventure.getUsersAllowedToWrite()).hasSameElementsAs(adventure.getUsersAllowedToWrite());
    }

    @Test
    public void emptyResultWhenAssetDoesntExistGettingByChannelId() {

        // Given
        String adventureId = "WRLDID";

        // When
        Optional<Adventure> retrievedAdventureOptional = repository.findByChannelId(adventureId);

        // Then
        assertThat(retrievedAdventureOptional).isNotNull().isEmpty();
    }

    @Test
    public void returnAllAdventuresWhenSearchingWithoutParameters() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.privateMultiplayerAdventure()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.privateMultiplayerAdventure()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .usersAllowedToRead(singleton(ownerId))
                        .build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.privateMultiplayerAdventure()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .build())
                .channelId("CHNLID3")
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchAdventures query = SearchAdventures.builder()
                .requesterId(ownerId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(1).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnAllAdventuresWhenSearchingWithoutParametersAsc() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini()
                        .aiModel(GPT35_TURBO)
                        .build())
                .channelId("CHNLID3")
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchAdventures query = SearchAdventures.builder()
                .requesterId(ownerId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(1).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void returnAllAdventuresWhenSearchingWithoutParametersDesc() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini()
                        .aiModel(GPT35_TURBO)
                        .build())
                .channelId("CHNLID3")
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchAdventures query = SearchAdventures.builder()
                .direction("DESC")
                .requesterId(ownerId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty();

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(adventures.get(1).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(2).getName()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchAdventureOrderByNameAsc() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 3")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini()
                        .aiModel(GPT35_TURBO)
                        .build())
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = SearchAdventures.builder()
                .sortingField("name")
                .requesterId(ownerId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty();

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(1).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchAdventureOrderByNameDesc() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 3")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini()
                        .aiModel(GPT35_TURBO)
                        .build())
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = SearchAdventures.builder()
                .sortingField("name")
                .direction("DESC")
                .requesterId(ownerId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty();

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(adventures.get(1).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(2).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureOrderByAiModelAsc() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = SearchAdventures.builder()
                .sortingField("modelConfiguration.aiModel")
                .direction("ASC")
                .requesterId(ownerId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(1).getName()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchAdventureOrderByAiModelDesc() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = SearchAdventures.builder()
                .sortingField("modelConfiguration.aiModel")
                .direction("DESC")
                .requesterId(ownerId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(1).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureOrderByModerationAsc() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .moderation(STRICT)
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .moderation(PERMISSIVE)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 3")
                .moderation(PERMISSIVE)
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = SearchAdventures.builder()
                .sortingField("moderation")
                .direction("ASC")
                .page(1)
                .size(10)
                .requesterId(ownerId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty();

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(1).getName()).isEqualTo(gpt354k.getName());
        assertThat(adventures.get(2).getName()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchAdventureOrderByModerationDesc() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = SearchAdventures.builder()
                .sortingField("modelConfiguration.aiModel")
                .direction("DESC")
                .page(1)
                .size(10)
                .requesterId(ownerId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(1).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureFilterByAiModel() {

        // Given
        String ownerId = "586678721356875";
        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = SearchAdventures.builder()
                .model("GPT4_MINI")
                .requesterId(ownerId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.getTotalItems()).isOne();
        assertThat(result.getTotalPages()).isOne();

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventure_whenReadAccess_andFilterByVisibility_thenReturnResults() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.privateMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = SearchAdventures.builder()
                .visibility("PRIVATE")
                .requesterId(ownerId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.getTotalItems()).isOne();
        assertThat(result.getTotalPages()).isOne();

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventure_whenWriteAccess_andFilterByVisibility_thenReturnResults() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.privateMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = SearchAdventures.builder()
                .visibility("PRIVATE")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.getTotalItems()).isOne();
        assertThat(result.getTotalPages()).isOne();

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureFilterByName() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 3")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini()
                        .aiModel(GPT35_TURBO)
                        .build())
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = SearchAdventures.builder()
                .name("Number 2")
                .requesterId(ownerId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.getTotalItems()).isOne();
        assertThat(result.getTotalPages()).isOne();

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureFilterByModeration() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .moderation(STRICT)
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .moderation(PERMISSIVE)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 3")
                .moderation(PERMISSIVE)
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = SearchAdventures.builder()
                .moderation("PERMISSIVE")
                .requesterId(ownerId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures).extracting(AdventureDetails::getName)
                .containsExactlyInAnyOrder(gpt4Mini.getName(), gpt354k.getName());
    }

    @Test
    public void searchAdventures_whenFilterByWorldId_andReaderOnly_thenReturnResults() {

        // Given
        String ownerId = "586678721356875";
        String worldId = "WRLD";
        Adventure gpt4Omni = jpaRepository.save(AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .worldId(worldId)
                .build());

        Adventure gpt4Mini = jpaRepository.save(AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .worldId("AAAA")
                .build());

        SearchAdventures query = SearchAdventures.builder()
                .requesterId(ownerId)
                .world(worldId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.getTotalItems()).isOne();
        assertThat(result.getTotalPages()).isOne();
    }

    @Test
    public void searchAdventures_whenFilterByPersonaId_andReaderOnly_thenReturnResults() {

        // Given
        String ownerId = "586678721356875";
        String personaId = "strict";
        Adventure gpt4Omni = jpaRepository.save(AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .personaId(personaId)
                .build());

        Adventure gpt4Mini = jpaRepository.save(AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .personaId("AAAA")
                .build());

        SearchAdventures query = SearchAdventures.builder()
                .requesterId(ownerId)
                .persona(personaId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.getTotalItems()).isOne();
        assertThat(result.getTotalPages()).isOne();
    }

    @Test
    public void returnAllAdventuresWhenSearchingWithoutParametersShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .build())
                .channelId("CHNLID3")
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchAdventures query = SearchAdventures.builder()
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(1).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnAllAdventuresWhenSearchingWithoutParametersAscShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini()
                        .aiModel(GPT35_TURBO)
                        .build())
                .channelId("CHNLID3")
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchAdventures query = SearchAdventures.builder()
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(1).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnAllAdventuresWhenSearchingWithoutParametersDescShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini()
                        .aiModel(GPT35_TURBO)
                        .build())
                .channelId("CHNLID3")
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchAdventures query = SearchAdventures.builder()
                .direction("DESC")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(1).getName()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchAdventureOrderByNameAscShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 3")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini()
                        .aiModel(GPT35_TURBO)
                        .build())
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = SearchAdventures.builder()
                .sortingField("name")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(1).getName()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchAdventureOrderByNameDescShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 3")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini()
                        .aiModel(GPT35_TURBO)
                        .build())
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = SearchAdventures.builder()
                .sortingField("name")
                .direction("DESC")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(1).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureOrderByAiModelAscShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = SearchAdventures.builder()
                .sortingField("modelConfiguration.aiModel")
                .direction("ASC")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.getTotalItems()).isOne();
        assertThat(result.getTotalPages()).isOne();

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureOrderByAiModelDescShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = SearchAdventures.builder()
                .sortingField("modelConfiguration.aiModel")
                .direction("DESC")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.getTotalItems()).isOne();
        assertThat(result.getTotalPages()).isOne();

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureOrderByModerationAscShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .moderation(STRICT)
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .moderation(PERMISSIVE)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 3")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .moderation(PERMISSIVE)
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = SearchAdventures.builder()
                .sortingField("moderation")
                .direction("ASC")
                .page(1)
                .size(10)
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(1).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchAdventureOrderByModerationDescShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = SearchAdventures.builder()
                .sortingField("modelConfiguration.aiModel")
                .direction("DESC")
                .page(1)
                .size(10)
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.getTotalItems()).isOne();
        assertThat(result.getTotalPages()).isOne();

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureFilterByAiModelShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 3")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini()
                        .aiModel(GPT35_TURBO)
                        .build())
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = SearchAdventures.builder()
                .model("GPT35_TURBO")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.getTotalItems()).isOne();
        assertThat(result.getTotalPages()).isOne();

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchAdventureFilterByNameShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 3")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini()
                        .aiModel(GPT35_TURBO)
                        .build())
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = SearchAdventures.builder()
                .name("Number 2")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.getTotalItems()).isOne();
        assertThat(result.getTotalPages()).isOne();

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureFilterByModerationShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .moderation(STRICT)
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .moderation(PERMISSIVE)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 3")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .moderation(PERMISSIVE)
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = SearchAdventures.builder()
                .moderation("PERMISSIVE")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(1).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchAdventureFilterByGameMode() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .moderation(STRICT)
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .gameMode(CHAT)
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .moderation(PERMISSIVE)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .gameMode(RPG)
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 3")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .moderation(PERMISSIVE)
                .channelId("CHNLID3")
                .gameMode(AUTHOR)
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = SearchAdventures.builder()
                .gameMode("RPG")
                .requesterId(ownerId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.getTotalItems()).isOne();
        assertThat(result.getTotalPages()).isOne();

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureFilterByGameModeShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .moderation(STRICT)
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .gameMode(AUTHOR)
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .ownerId(ownerId)
                        .build())
                .moderation(PERMISSIVE)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .gameMode(CHAT)
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 3")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .moderation(PERMISSIVE)
                .channelId("CHNLID3")
                .gameMode(RPG)
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = SearchAdventures.builder()
                .gameMode("CHAT")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.getTotalItems()).isOne();
        assertThat(result.getTotalPages()).isOne();

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureFilterByOwner() {

        // Given
        String ownerId = "586678721358363";
        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .moderation(STRICT)
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .gameMode(AUTHOR)
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .moderation(PERMISSIVE)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .gameMode(CHAT)
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 3")
                .moderation(PERMISSIVE)
                .channelId("CHNLID3")
                .gameMode(RPG)
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = SearchAdventures.builder()
                .ownerId(ownerId)
                .requesterId(ownerId)
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.getTotalItems()).isOne();
        assertThat(result.getTotalPages()).isOne();

        List<AdventureDetails> adventures = result.getResults();
        assertThat(adventures.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventures_whenFilterByWorldId_andWriterOnly_thenReturnResults() {

        // Given
        String ownerId = "586678721356875";
        String worldId = "WRLD";
        Adventure gpt4Omni = jpaRepository.save(AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .worldId(worldId)
                .build());

        Adventure gpt4Mini = jpaRepository.save(AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .worldId("AAAA")
                .build());

        SearchAdventures query = SearchAdventures.builder()
                .requesterId(ownerId)
                .world(worldId)
                .operation("WRITE")
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.getTotalItems()).isOne();
        assertThat(result.getTotalPages()).isOne();
    }

    @Test
    public void searchAdventures_whenFilterByPersonaId_andWriterOnly_thenReturnResults() {

        // Given
        String ownerId = "586678721356875";
        String personaId = "strict";
        Adventure gpt4Omni = jpaRepository.save(AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni().build())
                .channelId("CHNLID1")
                .personaId(personaId)
                .build());

        Adventure gpt4Mini = jpaRepository.save(AdventureFixture.publicMultiplayerAdventure()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini().build())
                .channelId("CHNLID2")
                .personaId("AAAA")
                .build());

        SearchAdventures query = SearchAdventures.builder()
                .requesterId(ownerId)
                .persona(personaId)
                .operation("WRITE")
                .build();

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.getTotalItems()).isOne();
        assertThat(result.getTotalPages()).isOne();
    }


    @Test
    public void adventure_whenChannelIdIsProvided_thenReturnGameMode() {

        // Given
        String channelId = "1234";
        Adventure adventure = jpaRepository.save(AdventureFixture.publicMultiplayerAdventure()
                .channelId(channelId)
                .build());

        // When
        String gameMode = repository.getGameModeByChannelId(channelId);

        // Then
        assertThat(gameMode).isNotNull()
                .isNotEmpty()
                .isEqualTo(adventure.getGameMode().name());
    }
}
