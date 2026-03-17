package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import static java.util.Collections.singleton;
import static me.moirai.storyengine.common.enums.GameMode.AUTHOR;
import static me.moirai.storyengine.common.enums.GameMode.CHAT;
import static me.moirai.storyengine.common.enums.GameMode.RPG;
import static me.moirai.storyengine.common.enums.Moderation.PERMISSIVE;
import static me.moirai.storyengine.common.enums.Moderation.STRICT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.ModelConfigurationFixture;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventuresResult;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

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
                .build();

        // When
        Adventure createdAdventure = repository.save(adventure);

        // Then
        assertThat(createdAdventure).isNotNull();

        assertThat(createdAdventure.getCreationDate()).isNotNull();
        assertThat(createdAdventure.getLastUpdateDate()).isNotNull();

        assertThat(createdAdventure.getModelConfiguration().aiModel().toString())
                .isEqualTo((adventure.getModelConfiguration().aiModel().toString()));

        assertThat(createdAdventure.getModelConfiguration().frequencyPenalty())
                .isEqualTo((adventure.getModelConfiguration().frequencyPenalty()));

        assertThat(createdAdventure.getModelConfiguration().presencePenalty())
                .isEqualTo((adventure.getModelConfiguration().presencePenalty()));

        assertThat(createdAdventure.getModelConfiguration().temperature())
                .isEqualTo((adventure.getModelConfiguration().temperature()));

        assertThat(createdAdventure.getModelConfiguration().logitBias())
                .isEqualTo((adventure.getModelConfiguration().logitBias()));

        assertThat(createdAdventure.getModelConfiguration().maxTokenLimit())
                .isEqualTo((adventure.getModelConfiguration().maxTokenLimit()));

        assertThat(createdAdventure.getModelConfiguration().stopSequences())
                .isEqualTo((adventure.getModelConfiguration().stopSequences()));

    }

    @Test
    public void emptyResultWhenAssetDoesntExist() {

        // Given
        UUID adventureId = AdventureFixture.PUBLIC_ID;

        // When
        Optional<Adventure> retrievedAdventureOptional = repository.findByPublicId(adventureId);

        // Then
        assertThat(retrievedAdventureOptional).isNotNull().isEmpty();
    }

    @Test
    public void deleteAdventure() {

        // Given
        Adventure adventure = repository.save(AdventureFixture.privateMultiplayerAdventure()
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
                .build());

        Adventure worldToUbeUpdated = AdventureFixture.privateMultiplayerAdventure()
                .visibility(Visibility.PUBLIC)
                .build();

        ReflectionTestUtils.setField(worldToUbeUpdated, "id", originalAdventure.getId());
        ReflectionTestUtils.setField(worldToUbeUpdated, "publicId", originalAdventure.getPublicId());

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
                .channelId(channelId)
                .build());

        entityManager.flush();
        entityManager.clear();

        // When
        repository.updateRememberByChannelId(remember, channelId);

        // Then
        Adventure updatedAdventure = repository.findByChannelId(channelId).get();
        assertThat(updatedAdventure.getContextAttributes().remember()).isEqualTo(remember);
    }

    @Test
    @Transactional
    public void updateAuthorsNoteAdventure() {

        // Given
        String authorsNote = "new value";
        String channelId = "123123123";

        repository.save(AdventureFixture.privateMultiplayerAdventure()
                .channelId(channelId)
                .build());

        entityManager.flush();
        entityManager.clear();

        // When
        repository.updateAuthorsNoteByChannelId(authorsNote, channelId);

        // Then
        Adventure updatedAdventure = repository.findByChannelId(channelId).get();
        assertThat(updatedAdventure.getContextAttributes().authorsNote()).isEqualTo(authorsNote);
    }

    @Test
    @Transactional
    public void updateNudgeAdventure() {

        // Given
        String nudge = "new value";
        String channelId = "123123123";

        repository.save(AdventureFixture.privateMultiplayerAdventure()
                .channelId(channelId)
                .build());

        entityManager.flush();
        entityManager.clear();

        // When
        repository.updateNudgeByChannelId(nudge, channelId);

        // Then
        Adventure updatedAdventure = repository.findByChannelId(channelId).get();
        assertThat(updatedAdventure.getContextAttributes().nudge()).isEqualTo(nudge);
    }

    @Test
    @Transactional
    public void updateBumpAdventure() {

        // Given
        int bumpFrequency = 35;
        String bump = "new value";
        String channelId = "123123123";

        repository.save(AdventureFixture.privateMultiplayerAdventure()
                .channelId(channelId)
                .build());

        entityManager.flush();
        entityManager.clear();

        // When
        repository.updateBumpByChannelId(bump, bumpFrequency, channelId);

        // Then
        Adventure updatedAdventure = repository.findByChannelId(channelId).get();
        assertThat(updatedAdventure.getContextAttributes().bump()).isEqualTo(bump);
        assertThat(updatedAdventure.getContextAttributes().bumpFrequency()).isEqualTo(bumpFrequency);
    }

    @Test
    public void retrieveAdventureById() {

        // Given
        String channelId = "234234";
        Adventure adventure = jpaRepository.save(AdventureFixture.publicMultiplayerAdventure()
                .channelId(channelId)
                .build());

        // When
        Optional<Adventure> retrievedAdventureOptional = repository.findByPublicId(adventure.getPublicId());

        // Then
        assertThat(retrievedAdventureOptional).isNotNull().isNotEmpty();

        Adventure retrievedAdventure = retrievedAdventureOptional.get();
        assertThat(retrievedAdventure.getPublicId()).isEqualTo(adventure.getPublicId());
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

        assertThat(retrievedAdventure.getModelConfiguration().aiModel())
                .isEqualTo(adventure.getModelConfiguration().aiModel());
        assertThat(retrievedAdventure.getModelConfiguration().frequencyPenalty())
                .isEqualTo(adventure.getModelConfiguration().frequencyPenalty());
        assertThat(retrievedAdventure.getModelConfiguration().logitBias())
                .isEqualTo(adventure.getModelConfiguration().logitBias());
        assertThat(retrievedAdventure.getModelConfiguration().maxTokenLimit())
                .isEqualTo(adventure.getModelConfiguration().maxTokenLimit());
        assertThat(retrievedAdventure.getModelConfiguration().presencePenalty())
                .isEqualTo(adventure.getModelConfiguration().presencePenalty());
        assertThat(retrievedAdventure.getModelConfiguration().stopSequences())
                .isEqualTo(adventure.getModelConfiguration().stopSequences());
        assertThat(retrievedAdventure.getModelConfiguration().temperature())
                .isEqualTo(adventure.getModelConfiguration().temperature());

        assertThat(retrievedAdventure.getUsersAllowedToRead()).hasSameElementsAs(adventure.getUsersAllowedToRead());
        assertThat(retrievedAdventure.getUsersAllowedToWrite()).hasSameElementsAs(adventure.getUsersAllowedToWrite());
    }

    @Test
    public void retrieveAdventureByChannelId() {

        // Given
        String channelId = "234234";
        Adventure adventure = jpaRepository.save(AdventureFixture.publicMultiplayerAdventure()
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

        assertThat(retrievedAdventure.getModelConfiguration().aiModel())
                .isEqualTo(adventure.getModelConfiguration().aiModel());
        assertThat(retrievedAdventure.getModelConfiguration().frequencyPenalty())
                .isEqualTo(adventure.getModelConfiguration().frequencyPenalty());
        assertThat(retrievedAdventure.getModelConfiguration().logitBias())
                .isEqualTo(adventure.getModelConfiguration().logitBias());
        assertThat(retrievedAdventure.getModelConfiguration().maxTokenLimit())
                .isEqualTo(adventure.getModelConfiguration().maxTokenLimit());
        assertThat(retrievedAdventure.getModelConfiguration().presencePenalty())
                .isEqualTo(adventure.getModelConfiguration().presencePenalty());
        assertThat(retrievedAdventure.getModelConfiguration().stopSequences())
                .isEqualTo(adventure.getModelConfiguration().stopSequences());
        assertThat(retrievedAdventure.getModelConfiguration().temperature())
                .isEqualTo(adventure.getModelConfiguration().temperature());

        assertThat(retrievedAdventure.getUsersAllowedToRead()).hasSameElementsAs(adventure.getUsersAllowedToRead());
        assertThat(retrievedAdventure.getUsersAllowedToWrite()).hasSameElementsAs(adventure.getUsersAllowedToWrite());
    }

    @Test
    public void emptyResultWhenAssetDoesntExistGettingByChannelId() {

        // Given
        String channelId = "WRLDID";

        // When
        Optional<Adventure> retrievedAdventureOptional = repository.findByChannelId(channelId);

        // Then
        assertThat(retrievedAdventureOptional).isNotNull().isEmpty();
    }

    @Test
    public void returnAllAdventuresWhenSearchingWithoutParameters() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .usersAllowedToRead(singleton(ownerId))
                        .build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .build())
                .channelId("CHNLID3")
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, null, null, null, null, null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(1).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnAllAdventuresWhenSearchingWithoutParametersAsc() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .modelConfiguration(ModelConfigurationFixture.gpt35Turbo())
                .channelId("CHNLID3")
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, null, null, null, null, null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(3);

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(1).name()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(2).name()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void returnAllAdventuresWhenSearchingWithoutParametersDesc() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .modelConfiguration(ModelConfigurationFixture.gpt35Turbo())
                .channelId("CHNLID3")
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, null, null, "DESC", null, null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty();

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt354k.getName());
        assertThat(adventures.get(1).name()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(2).name()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchAdventureOrderByNameAsc() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 3")
                .modelConfiguration(ModelConfigurationFixture.gpt35Turbo())
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, null, "name", null, null, null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty();

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(1).name()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(2).name()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchAdventureOrderByNameDesc() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 3")
                .modelConfiguration(ModelConfigurationFixture.gpt35Turbo())
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, null, "name", "DESC", null, null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty();

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt354k.getName());
        assertThat(adventures.get(1).name()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(2).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureOrderByAiModelAsc() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, null, "modelConfiguration.aiModel", "ASC", null, null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(1).name()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchAdventureOrderByAiModelDesc() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, null, "modelConfiguration.aiModel", "DESC", null, null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(1).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureOrderByModerationAsc() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .moderation(STRICT)
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .moderation(PERMISSIVE)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 3")
                .moderation(PERMISSIVE)
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, 1, 10, null, null, null, "moderation", "ASC", null, null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty();

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(1).name()).isEqualTo(gpt354k.getName());
        assertThat(adventures.get(2).name()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchAdventureOrderByModerationDesc() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, 1, 10, null, null, null, "modelConfiguration.aiModel", "DESC", null, null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(1).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureFilterByAiModel() {

        // Given
        String ownerId = "586678721356875";
        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, "GPT4_MINI", null, null, null, null, null, null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.totalItems()).isOne();
        assertThat(result.totalPages()).isOne();

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventure_whenReadAccess_andFilterByVisibility_thenReturnResults() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.privateMultiplayerAdventure()
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, null, null, null, "PRIVATE", null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.totalItems()).isOne();
        assertThat(result.totalPages()).isOne();

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventure_whenWriteAccess_andFilterByVisibility_thenReturnResults() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.privateMultiplayerAdventure()
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, null, null, null, "PRIVATE", "WRITE", ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.totalItems()).isOne();
        assertThat(result.totalPages()).isOne();

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureFilterByName() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 3")
                .modelConfiguration(ModelConfigurationFixture.gpt35Turbo())
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = new SearchAdventures(
                "Number 2", null, null, null, false, null, null, null, null, null, null, null, null, null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.totalItems()).isOne();
        assertThat(result.totalPages()).isOne();

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureFilterByModeration() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .moderation(STRICT)
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .moderation(PERMISSIVE)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 3")
                .moderation(PERMISSIVE)
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, "PERMISSIVE", null, null, null, null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures).extracting(AdventureDetails::name)
                .containsExactlyInAnyOrder(gpt4Mini.getName(), gpt354k.getName());
    }

    @Test
    public void searchAdventures_whenFilterByWorldId_andReaderOnly_thenReturnResults() {

        // Given
        String ownerId = "586678721356875";
        Long worldIdValue = 999L;
        String worldId = "999";

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .worldId(worldIdValue)
                .channelId("CHNLID_WRLD_R")
                .build();

        jpaRepository.save(adventure);

        SearchAdventures query = new SearchAdventures(
                null, worldId, null, null, false, null, null, null, null, null, null, null, null, null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.totalItems()).isOne();
        assertThat(result.totalPages()).isOne();
    }

    @Test
    public void searchAdventures_whenFilterByPersonaId_andReaderOnly_thenReturnResults() {

        // Given
        String ownerId = "586678721356875";
        Long personaIdValue = 888L;
        String personaId = "888";

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .personaId(personaIdValue)
                .channelId("CHNLID_PSNA_R")
                .build();

        jpaRepository.save(adventure);

        SearchAdventures query = new SearchAdventures(
                null, null, personaId, null, false, null, null, null, null, null, null, null, null, null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.totalItems()).isOne();
        assertThat(result.totalPages()).isOne();
    }

    @Test
    public void returnAllAdventuresWhenSearchingWithoutParametersShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .build())
                .channelId("CHNLID3")
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, null, null, null, null, "WRITE", ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(1).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnAllAdventuresWhenSearchingWithoutParametersAscShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .modelConfiguration(ModelConfigurationFixture.gpt35Turbo())
                .channelId("CHNLID3")
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, null, null, null, null, "WRITE", ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(1).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnAllAdventuresWhenSearchingWithoutParametersDescShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .modelConfiguration(ModelConfigurationFixture.gpt35Turbo())
                .channelId("CHNLID3")
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, null, null, "DESC", null, "WRITE", ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(1).name()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchAdventureOrderByNameAscShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 3")
                .modelConfiguration(ModelConfigurationFixture.gpt35Turbo())
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, null, "name", null, null, "WRITE", ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(1).name()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchAdventureOrderByNameDescShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 3")
                .modelConfiguration(ModelConfigurationFixture.gpt35Turbo())
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, null, "name", "DESC", null, "WRITE", ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Omni.getName());
        assertThat(adventures.get(1).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureOrderByAiModelAscShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, null, "modelConfiguration.aiModel", "ASC", null, "WRITE", ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.totalItems()).isOne();
        assertThat(result.totalPages()).isOne();

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureOrderByAiModelDescShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, null, "modelConfiguration.aiModel", "DESC", null, "WRITE", ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.totalItems()).isOne();
        assertThat(result.totalPages()).isOne();

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureOrderByModerationAscShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .moderation(STRICT)
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .moderation(PERMISSIVE)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 3")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .moderation(PERMISSIVE)
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, 1, 10, null, null, null, "moderation", "ASC", null, "WRITE", ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(1).name()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchAdventureOrderByModerationDescShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, 1, 10, null, null, null, "modelConfiguration.aiModel", "DESC", null, "WRITE", ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.totalItems()).isOne();
        assertThat(result.totalPages()).isOne();

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureFilterByAiModelShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 3")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt35Turbo())
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, "GPT35_TURBO", null, null, null, null, null, "WRITE", ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.totalItems()).isOne();
        assertThat(result.totalPages()).isOne();

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchAdventureFilterByNameShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 3")
                .modelConfiguration(ModelConfigurationFixture.gpt35Turbo())
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = new SearchAdventures(
                "Number 2", null, null, null, false, null, null, null, null, null, null, null, null, "WRITE", ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.totalItems()).isOne();
        assertThat(result.totalPages()).isOne();

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureFilterByModerationShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .moderation(STRICT)
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .moderation(PERMISSIVE)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 3")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .moderation(PERMISSIVE)
                .channelId("CHNLID3")
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, null, "PERMISSIVE", null, null, null, "WRITE", ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
        assertThat(adventures.get(1).name()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchAdventureFilterByGameMode() {

        // Given
        String ownerId = "586678721356875";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .moderation(STRICT)
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .gameMode(CHAT)
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .moderation(PERMISSIVE)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .gameMode(RPG)
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 3")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .moderation(PERMISSIVE)
                .channelId("CHNLID3")
                .gameMode(AUTHOR)
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, "RPG", null, null, null, null, null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.totalItems()).isOne();
        assertThat(result.totalPages()).isOne();

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureFilterByGameModeShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .moderation(STRICT)
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .gameMode(AUTHOR)
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .ownerId(ownerId)
                        .build())
                .moderation(PERMISSIVE)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .gameMode(CHAT)
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 3")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(singleton(ownerId))
                        .build())
                .moderation(PERMISSIVE)
                .channelId("CHNLID3")
                .gameMode(RPG)
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = new SearchAdventures(
                null, null, null, null, false, null, null, null, "CHAT", null, null, null, null, "WRITE", ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.totalItems()).isOne();
        assertThat(result.totalPages()).isOne();

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventureFilterByOwner() {

        // Given
        String ownerId = "586678721358363";
        Adventure gpt4Omni = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 1")
                .moderation(STRICT)
                .modelConfiguration(ModelConfigurationFixture.gpt4Omni())
                .channelId("CHNLID1")
                .gameMode(AUTHOR)
                .build();

        Adventure gpt4Mini = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .moderation(PERMISSIVE)
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .channelId("CHNLID2")
                .gameMode(CHAT)
                .build();

        Adventure gpt354k = AdventureFixture.publicMultiplayerAdventure()
                .name("Number 3")
                .moderation(PERMISSIVE)
                .channelId("CHNLID3")
                .gameMode(RPG)
                .build();

        jpaRepository.saveAll(list(gpt4Omni, gpt4Mini, gpt354k));

        SearchAdventures query = new SearchAdventures(
                null, null, null, ownerId, false, null, null, null, null, null, null, null, null, null, ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.totalItems()).isOne();
        assertThat(result.totalPages()).isOne();

        List<AdventureDetails> adventures = result.results();
        assertThat(adventures.get(0).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchAdventures_whenFilterByWorldId_andWriterOnly_thenReturnResults() {

        // Given
        String ownerId = "586678721356875";
        Long worldIdValue = 999L;
        String worldId = "999";

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .worldId(worldIdValue)
                .channelId("CHNLID_WRLD_W")
                .build();

        jpaRepository.save(adventure);

        SearchAdventures query = new SearchAdventures(
                null, worldId, null, null, false, null, null, null, null, null, null, null, null, "WRITE", ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.totalItems()).isOne();
        assertThat(result.totalPages()).isOne();
    }

    @Test
    public void searchAdventures_whenFilterByPersonaId_andWriterOnly_thenReturnResults() {

        // Given
        String ownerId = "586678721356875";
        Long personaIdValue = 888L;
        String personaId = "888";

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .personaId(personaIdValue)
                .channelId("CHNLID_PSNA_W")
                .build();

        jpaRepository.save(adventure);

        SearchAdventures query = new SearchAdventures(
                null, null, personaId, null, false, null, null, null, null, null, null, null, null, "WRITE", ownerId);

        // When
        SearchAdventuresResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(result.totalItems()).isOne();
        assertThat(result.totalPages()).isOne();
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
