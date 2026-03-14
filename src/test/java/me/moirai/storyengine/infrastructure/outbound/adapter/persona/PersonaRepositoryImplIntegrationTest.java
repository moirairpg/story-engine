package me.moirai.storyengine.infrastructure.outbound.adapter.persona;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.set;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.transaction.Transactional;
import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.common.domain.Visibility;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;
import me.moirai.storyengine.core.port.inbound.persona.GetPersonaResult;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonasResult;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaRepository;
import me.moirai.storyengine.infrastructure.outbound.adapter.favorite.FavoriteEntity;
import me.moirai.storyengine.infrastructure.outbound.adapter.favorite.FavoriteRepository;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;

public class PersonaRepositoryImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PersonaRepository repository;

    @Autowired
    private PersonaJpaRepository jpaRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @BeforeEach
    public void before() {
        jpaRepository.deleteAllInBatch();
    }

    @Test
    public void retrievePersonaById() {

        // Given
        Persona persona = jpaRepository.save(PersonaFixture.privatePersona()
                .id(null)
                .build());

        // When
        Optional<Persona> retrievedPersonaOptional = repository.findById(persona.getId());

        // Then
        assertThat(retrievedPersonaOptional).isNotNull().isNotEmpty();

        Persona retrievedPersona = retrievedPersonaOptional.get();
        assertThat(retrievedPersona.getId()).isEqualTo(persona.getId());
    }

    @Test
    public void createPersona() {

        // Given
        Persona persona = PersonaFixture.privatePersona()
                .id(null)
                .build();

        // When
        Persona createdPersona = repository.save(persona);

        // Then
        assertThat(createdPersona).isNotNull();

        assertThat(createdPersona.getCreationDate()).isNotNull();
        assertThat(createdPersona.getLastUpdateDate()).isNotNull();

        assertThat(createdPersona.getName()).isEqualTo(persona.getName());
        assertThat(createdPersona.getPersonality()).isEqualTo(persona.getPersonality());
        assertThat(createdPersona.getVisibility()).isEqualTo(persona.getVisibility());
        assertThat(createdPersona.getUsersAllowedToWrite()).hasSameElementsAs(persona.getUsersAllowedToWrite());
        assertThat(createdPersona.getUsersAllowedToRead()).hasSameElementsAs(persona.getUsersAllowedToRead());
    }

    @Test
    public void existsById_ifPersonaExists_thenReturnTrue() {

        // Given
        Persona createdPersona = repository.save(PersonaFixture.publicPersona().build());

        // When
        boolean result = repository.existsById(createdPersona.getId());

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void existsById_ifPersonaNotExists_thenReturnFalse() {

        // Given
        String personaId = "InvalidId";

        // When
        boolean result = repository.existsById(personaId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void deletePersona() {

        // Given
        Persona persona = repository.save(PersonaFixture.privatePersona()
                .id(null)
                .build());

        // When
        repository.deleteById(persona.getId());

        // Then
        assertThat(jpaRepository.findById(persona.getId())).isNotNull().isEmpty();
    }

    @Test
    public void updatePersona() {

        // Given
        Persona originalPersona = repository.save(PersonaFixture.privatePersona()
                .id(null)
                .build());

        Persona worldToUbeUpdated = PersonaFixture.privatePersona()
                .id(originalPersona.getId())
                .visibility(Visibility.PUBLIC)
                .version(originalPersona.getVersion())
                .build();

        // When
        Persona updatedPersona = repository.save(worldToUbeUpdated);

        // Then
        assertThat(originalPersona.getVersion()).isZero();
        assertThat(updatedPersona.getVersion()).isOne();
    }

    @Test
    @Transactional
    public void deletePersona_whenIsFavorite_thenDeleteFavorites() {

        // Given
        String userId = "1234";
        Persona originalPersona = repository.save(PersonaFixture.privatePersona()
                .id(null)
                .build());

        FavoriteEntity favorite = favoriteRepository.save(FavoriteEntity.builder()
                .playerId(userId)
                .assetId(originalPersona.getId())
                .assetType("persona")
                .build());

        // When
        repository.deleteById(originalPersona.getId());

        // Then
        assertThat(repository.findById(originalPersona.getId())).isNotNull().isEmpty();
        assertThat(favoriteRepository.existsById(favorite.getId())).isFalse();
    }

    @Test
    public void emptyResultWhenAssetDoesntExist() {

        // Given
        String personaId = "PRSNDID";

        // When
        Optional<Persona> retrievedPersonaOptional = repository.findById(personaId);

        // Then
        assertThat(retrievedPersonaOptional).isNotNull().isEmpty();
    }

    @Test
    public void returnAllPersonasWhenSearchingWithoutParameters() {

        // Given
        String ownerId = "586678721356875";

        Persona gpt4Omni = PersonaFixture.privatePersona()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .usersAllowedToRead(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .build())
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchPersonas query = SearchPersonas.builder()
                .requesterId(ownerId)
                .build();

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetPersonaResult> personas = result.getResults();
        assertThat(personas.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(personas.get(1).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnAllPersonasWhenSearchingWithoutParametersAsc() {

        // Given
        String ownerId = "586678721356875";

        Persona gpt4Omni = PersonaFixture.privatePersona()
                .id(null)
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()
                .id(null)
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()
                .id(null)
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchPersonas query = SearchPersonas.builder()
                .requesterId(ownerId)
                .build();

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetPersonaResult> personas = result.getResults();
        assertThat(personas.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(personas.get(1).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(personas.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void returnAllPersonasWhenSearchingWithoutParametersDesc() {

        // Given
        String ownerId = "586678721356875";

        Persona gpt4Omni = PersonaFixture.privatePersona()
                .id(null)
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()
                .id(null)
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()
                .id(null)
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchPersonas query = SearchPersonas.builder()
                .direction("DESC")
                .page(1)
                .size(10)
                .requesterId(ownerId)
                .build();

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetPersonaResult> personas = result.getResults();
        assertThat(personas.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(personas.get(1).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(personas.get(2).getName()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchPersonaOrderByNameAsc() {

        // Given
        String ownerId = "586678721356875";

        Persona gpt4Omni = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 2")
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 1")
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = SearchPersonas.builder()
                .sortingField("name")
                .requesterId(ownerId)
                .build();

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetPersonaResult> personas = result.getResults();
        assertThat(personas.get(0).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(personas.get(1).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(personas.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchPersonaOrderByNameDesc() {

        // Given
        String ownerId = "586678721356875";

        Persona gpt4Omni = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 2")
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 1")
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = SearchPersonas.builder()
                .sortingField("name")
                .direction("DESC")
                .requesterId(ownerId)
                .build();

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetPersonaResult> personas = result.getResults();
        assertThat(personas.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(personas.get(1).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(personas.get(2).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchPersonaFilterByName() {

        // Given
        String ownerId = "586678721356875";

        Persona gpt4Omni = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 1")
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 2")
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = SearchPersonas.builder()
                .name("Number 2")
                .requesterId(ownerId)
                .build();

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<GetPersonaResult> personas = result.getResults();
        assertThat(personas.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchPersonaFilterByOwner() {

        // Given
        String ownerId = "586678721356875";

        Persona gpt4Omni = PersonaFixture.publicPersona()
                .id(null)
                .name("Number 1")
                .build();

        Persona gpt4Mini = PersonaFixture.publicPersona()
                .id(null)
                .name("Number 2")
                .build();

        Persona gpt354k = PersonaFixture.publicPersona()
                .id(null)
                .name("Number 3")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = SearchPersonas.builder()
                .name("Number 2")
                .requesterId(ownerId)
                .ownerId(ownerId)
                .build();

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<GetPersonaResult> personas = result.getResults();
        assertThat(personas.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnAllPersonasWhenSearchingWithoutParametersShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.privatePersona()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .build())
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchPersonas query = SearchPersonas.builder()
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetPersonaResult> personas = result.getResults();
        assertThat(personas.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(personas.get(1).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnAllPersonasWhenSearchingWithoutParametersAscShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.privatePersona()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()
                .id(null)
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchPersonas query = SearchPersonas.builder()
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetPersonaResult> personas = result.getResults();
        assertThat(personas.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(personas.get(1).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnAllPersonasWhenSearchingWithoutParametersDescShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.privatePersona()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()
                .id(null)
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()
                .id(null)
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchPersonas query = SearchPersonas.builder()
                .direction("DESC")
                .page(1)
                .size(10)
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetPersonaResult> personas = result.getResults();
        assertThat(personas.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(personas.get(1).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchPersonaOrderByNameAscShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = SearchPersonas.builder()
                .sortingField("name")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetPersonaResult> personas = result.getResults();
        assertThat(personas.get(0).getName()).isEqualTo(gpt4Mini.getName());
        assertThat(personas.get(1).getName()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchPersonaOrderByNameDescShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = SearchPersonas.builder()
                .sortingField("name")
                .direction("DESC")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetPersonaResult> personas = result.getResults();
        assertThat(personas.get(0).getName()).isEqualTo(gpt4Omni.getName());
        assertThat(personas.get(1).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchPersonaFilterByNameShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 1")
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = SearchPersonas.builder()
                .name("Number 2")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<GetPersonaResult> personas = result.getResults();
        assertThat(personas.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchPersonas_whenWritingAccess_andFilterByVisibility_thenReturnResults() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.publicPersona()
                .id(null)
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = SearchPersonas.builder()
                .requesterId(ownerId)
                .visibility("private")
                .operation("WRITE")
                .build();

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<GetPersonaResult> personas = result.getResults();
        assertThat(personas.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchPersonas_whenReadingAccess_andFilterByVisibility_thenReturnResults() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.publicPersona()
                .id(null)
                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToRead(set(ownerId))
                        .build())
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToRead(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = SearchPersonas.builder()
                .requesterId(ownerId)
                .visibility("private")
                .operation("READ")
                .build();

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<GetPersonaResult> personas = result.getResults();
        assertThat(personas.get(0).getName()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void emptyResultWhenSearchingForPersonaWithWriteAccessIfUserHasNoAccess() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 1")
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 2")
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = SearchPersonas.builder()
                .name("Number 2")
                .requesterId(ownerId)
                .operation("WRITE")
                .build();

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isEmpty();
    }

    @Test
    public void getFavorites_whenNoFilters_thenReturnAll() {

        // Given
        String playerId = "63456456";
        SearchPersonas request = SearchPersonas.builder()
                .requesterId(playerId)
                .favorites(true)
                .build();

        Persona persona1 = jpaRepository.save(PersonaFixture.publicPersona()
                .id(null)
                .name("Number 1")
                .build());

        Persona persona2 = jpaRepository.save(PersonaFixture.publicPersona()
                .id(null)
                .name("Number 2")
                .build());

        Persona persona3 = jpaRepository.save(PersonaFixture.publicPersona()
                .id(null)
                .name("Number 3")
                .build());

        FavoriteEntity favorite1 = FavoriteEntity.builder()
                .playerId(playerId)
                .assetType("persona")
                .assetId(persona1.getId())
                .build();

        FavoriteEntity favorite2 = FavoriteEntity.builder()
                .playerId(playerId)
                .assetType("persona")
                .assetId(persona2.getId())
                .build();

        FavoriteEntity favorite3 = FavoriteEntity.builder()
                .playerId(playerId)
                .assetType("persona")
                .assetId(persona3.getId())
                .build();

        favoriteRepository.saveAll(set(favorite1, favorite2, favorite3));

        // When
        SearchPersonasResult result = repository.search(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().hasSize(3);
    }

    @Test
    public void getFavorites_whenFilterByName_thenReturnAll() {

        // Given
        String nameToSearch = "targetName";
        String playerId = "63456456";
        SearchPersonas request = SearchPersonas.builder()
                .requesterId(playerId)
                .name(nameToSearch)
                .favorites(true)
                .build();

        Persona persona1 = jpaRepository.save(PersonaFixture.publicPersona()
                .id(null)
                .name(nameToSearch)
                .build());

        Persona persona2 = jpaRepository.save(PersonaFixture.publicPersona()
                .id(null)
                .name("Number 2")
                .build());

        Persona persona3 = jpaRepository.save(PersonaFixture.publicPersona()
                .id(null)
                .name("Number 3")
                .build());

        FavoriteEntity favorite1 = FavoriteEntity.builder()
                .playerId(playerId)
                .assetType("persona")
                .assetId(persona1.getId())
                .build();

        FavoriteEntity favorite2 = FavoriteEntity.builder()
                .playerId(playerId)
                .assetType("persona")
                .assetId(persona2.getId())
                .build();

        FavoriteEntity favorite3 = FavoriteEntity.builder()
                .playerId(playerId)
                .assetType("persona")
                .assetId(persona3.getId())
                .build();

        favoriteRepository.saveAll(set(favorite1, favorite2, favorite3));

        // When
        SearchPersonasResult result = repository.search(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().hasSize(1);
    }

    @Test
    public void getFavorites_whenFilterByVisibility_thenReturnAll() {

        // Given
        String playerId = "63456456";
        SearchPersonas request = SearchPersonas.builder()
                .requesterId(playerId)
                .visibility("public")
                .favorites(true)
                .build();

        Persona persona1 = jpaRepository.save(PersonaFixture.publicPersona()
                .id(null)
                .name("Number 1")
                .build());

        Persona persona2 = jpaRepository.save(PersonaFixture.publicPersona()
                .id(null)
                .name("Number 2")
                .build());

        Persona persona3 = jpaRepository.save(PersonaFixture.privatePersona()
                .id(null)
                .name("Number 3")
                .build());

        FavoriteEntity favorite1 = FavoriteEntity.builder()
                .playerId(playerId)
                .assetType("persona")
                .assetId(persona1.getId())
                .build();

        FavoriteEntity favorite2 = FavoriteEntity.builder()
                .playerId(playerId)
                .assetType("persona")
                .assetId(persona2.getId())
                .build();

        FavoriteEntity favorite3 = FavoriteEntity.builder()
                .playerId(playerId)
                .assetType("persona")
                .assetId(persona3.getId())
                .build();

        favoriteRepository.saveAll(set(favorite1, favorite2, favorite3));

        // When
        SearchPersonasResult result = repository.search(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().hasSize(2);
    }
}
