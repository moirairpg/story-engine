package me.moirai.storyengine.infrastructure.outbound.adapter.persona;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.set;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonasResult;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

public class PersonaRepositoryImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PersonaRepository repository;

    @Autowired
    private PersonaJpaRepository jpaRepository;

    @BeforeEach
    public void before() {
        jpaRepository.deleteAllInBatch();
    }

    @Test
    public void retrievePersonaById() {

        // Given
        Persona persona = jpaRepository.save(PersonaFixture.privatePersona().build());

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
        Persona persona = PersonaFixture.privatePersona().build();

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
        UUID personaId = UUID.randomUUID();

        // When
        boolean result = repository.existsByPublicId(personaId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void deletePersona() {

        // Given
        Persona persona = repository.save(PersonaFixture.privatePersona().build());

        // When
        repository.deleteById(persona.getId());

        // Then
        assertThat(jpaRepository.findById(persona.getId())).isNotNull().isEmpty();
    }

    @Test
    public void updatePersona() {

        // Given
        Persona originalPersona = repository.save(PersonaFixture.privatePersona().build());

        Persona personaTobeUpdated = PersonaFixture.privatePersona()
                .visibility(Visibility.PUBLIC)
                .build();

        ReflectionTestUtils.setField(personaTobeUpdated, "id", originalPersona.getId());
        ReflectionTestUtils.setField(personaTobeUpdated, "publicId", originalPersona.getPublicId());

        // When
        Persona updatedPersona = repository.save(personaTobeUpdated);

        // Then
        assertThat(originalPersona.getVersion()).isZero();
        assertThat(updatedPersona.getVersion()).isOne();
    }

    @Test
    public void emptyResultWhenAssetDoesntExist() {

        // Given
        UUID personaId = PersonaFixture.PUBLIC_ID;

        // When
        Optional<Persona> retrievedPersonaOptional = repository.findByPublicId(personaId);

        // Then
        assertThat(retrievedPersonaOptional).isNotNull().isEmpty();
    }

    @Test
    public void returnAllPersonasWhenSearchingWithoutParameters() {

        // Given
        String ownerId = "586678721356875";

        Persona gpt4Omni = PersonaFixture.privatePersona()

                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()

                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .usersAllowedToRead(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()

                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .build())
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchPersonas query = new SearchPersonas(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ownerId);

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<PersonaDetails> personas = result.results();
        assertThat(personas.get(0).name()).isEqualTo(gpt4Omni.getName());
        assertThat(personas.get(1).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnAllPersonasWhenSearchingWithoutParametersAsc() {

        // Given
        String ownerId = "586678721356875";

        Persona gpt4Omni = PersonaFixture.privatePersona()

                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()

                .build();

        Persona gpt354k = PersonaFixture.privatePersona()

                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchPersonas query = new SearchPersonas(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ownerId);

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(3);

        List<PersonaDetails> personas = result.results();
        assertThat(personas.get(0).name()).isEqualTo(gpt4Omni.getName());
        assertThat(personas.get(1).name()).isEqualTo(gpt4Mini.getName());
        assertThat(personas.get(2).name()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void returnAllPersonasWhenSearchingWithoutParametersDesc() {

        // Given
        String ownerId = "586678721356875";

        Persona gpt4Omni = PersonaFixture.privatePersona()

                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()

                .build();

        Persona gpt354k = PersonaFixture.privatePersona()

                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchPersonas query = new SearchPersonas(
                null,
                null,
                1,
                10,
                null,
                "DESC",
                null,
                null,
                ownerId);

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(3);

        List<PersonaDetails> personas = result.results();
        assertThat(personas.get(0).name()).isEqualTo(gpt354k.getName());
        assertThat(personas.get(1).name()).isEqualTo(gpt4Mini.getName());
        assertThat(personas.get(2).name()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchPersonaOrderByNameAsc() {

        // Given
        String ownerId = "586678721356875";

        Persona gpt4Omni = PersonaFixture.privatePersona()

                .name("Number 2")
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()

                .name("Number 1")
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()

                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = new SearchPersonas(
                null,
                null,
                null,
                null,
                "name",
                null,
                null,
                null,
                ownerId);

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(3);

        List<PersonaDetails> personas = result.results();
        assertThat(personas.get(0).name()).isEqualTo(gpt4Mini.getName());
        assertThat(personas.get(1).name()).isEqualTo(gpt4Omni.getName());
        assertThat(personas.get(2).name()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchPersonaOrderByNameDesc() {

        // Given
        String ownerId = "586678721356875";

        Persona gpt4Omni = PersonaFixture.privatePersona()

                .name("Number 2")
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()

                .name("Number 1")
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()

                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = new SearchPersonas(
                null,
                null,
                null,
                null,
                "name",
                "DESC",
                null,
                null,
                ownerId);

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(3);

        List<PersonaDetails> personas = result.results();
        assertThat(personas.get(0).name()).isEqualTo(gpt354k.getName());
        assertThat(personas.get(1).name()).isEqualTo(gpt4Omni.getName());
        assertThat(personas.get(2).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchPersonaFilterByName() {

        // Given
        String ownerId = "586678721356875";

        Persona gpt4Omni = PersonaFixture.privatePersona()

                .name("Number 1")
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()

                .name("Number 2")
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()

                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = new SearchPersonas(
                "Number 2",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ownerId);

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);

        List<PersonaDetails> personas = result.results();
        assertThat(personas.get(0).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchPersonaFilterByOwner() {

        // Given
        String ownerId = "586678721356875";

        Persona gpt4Omni = PersonaFixture.publicPersona()

                .name("Number 1")
                .build();

        Persona gpt4Mini = PersonaFixture.publicPersona()

                .name("Number 2")
                .build();

        Persona gpt354k = PersonaFixture.publicPersona()

                .name("Number 3")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = new SearchPersonas(
                "Number 2",
                ownerId,
                null,
                null,
                null,
                null,
                null,
                null,
                ownerId);

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);

        List<PersonaDetails> personas = result.results();
        assertThat(personas.get(0).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnAllPersonasWhenSearchingWithoutParametersShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.privatePersona()

                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()

                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()

                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("580485734")
                        .build())
                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchPersonas query = new SearchPersonas(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "WRITE",
                ownerId);

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<PersonaDetails> personas = result.results();
        assertThat(personas.get(0).name()).isEqualTo(gpt4Omni.getName());
        assertThat(personas.get(1).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnAllPersonasWhenSearchingWithoutParametersAscShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.privatePersona()

                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()

                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()

                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchPersonas query = new SearchPersonas(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "WRITE",
                ownerId);

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<PersonaDetails> personas = result.results();
        assertThat(personas.get(0).name()).isEqualTo(gpt4Omni.getName());
        assertThat(personas.get(1).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void returnAllPersonasWhenSearchingWithoutParametersDescShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.privatePersona()

                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()

                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()

                .build();

        jpaRepository.save(gpt4Omni);
        jpaRepository.save(gpt4Mini);
        jpaRepository.save(gpt354k);

        SearchPersonas query = new SearchPersonas(
                null,
                null,
                1,
                10,
                null,
                "DESC",
                null,
                "WRITE",
                ownerId);

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<PersonaDetails> personas = result.results();
        assertThat(personas.get(0).name()).isEqualTo(gpt354k.getName());
        assertThat(personas.get(1).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchPersonaOrderByNameAscShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.privatePersona()

                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()

                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()

                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = new SearchPersonas(
                null,
                null,
                null,
                null,
                "name",
                null,
                null,
                "WRITE",
                ownerId);

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<PersonaDetails> personas = result.results();
        assertThat(personas.get(0).name()).isEqualTo(gpt4Mini.getName());
        assertThat(personas.get(1).name()).isEqualTo(gpt4Omni.getName());
    }

    @Test
    public void searchPersonaOrderByNameDescShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.privatePersona()

                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(ownerId)
                        .build())
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()

                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()

                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = new SearchPersonas(
                null,
                null,
                null,
                null,
                "name",
                "DESC",
                null,
                "WRITE",
                ownerId);

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(2);

        List<PersonaDetails> personas = result.results();
        assertThat(personas.get(0).name()).isEqualTo(gpt4Omni.getName());
        assertThat(personas.get(1).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchPersonaFilterByNameShowOnlyWithWriteAccess() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.privatePersona()

                .name("Number 1")
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()

                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()

                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = new SearchPersonas(
                "Number 2",
                null,
                null,
                null,
                null,
                null,
                null,
                "WRITE",
                ownerId);

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);

        List<PersonaDetails> personas = result.results();
        assertThat(personas.get(0).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchPersonas_whenWritingAccess_andFilterByVisibility_thenReturnResults() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.publicPersona()

                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()

                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToWrite(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()

                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = new SearchPersonas(
                null,
                null,
                null,
                null,
                null,
                null,
                Visibility.PRIVATE,
                "WRITE",
                ownerId);

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);

        List<PersonaDetails> personas = result.results();
        assertThat(personas.get(0).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void searchPersonas_whenReadingAccess_andFilterByVisibility_thenReturnResults() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.publicPersona()

                .name("Number 1")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToRead(set(ownerId))
                        .build())
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()

                .name("Number 2")
                .permissions(PermissionsFixture.samplePermissions()
                        .usersAllowedToRead(set(ownerId))
                        .build())
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()

                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = new SearchPersonas(
                null,
                null,
                null,
                null,
                null,
                null,
                Visibility.PRIVATE,
                "READ",
                ownerId);

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isNotEmpty().hasSize(1);

        List<PersonaDetails> personas = result.results();
        assertThat(personas.get(0).name()).isEqualTo(gpt4Mini.getName());
    }

    @Test
    public void emptyResultWhenSearchingForPersonaWithWriteAccessIfUserHasNoAccess() {

        // Given
        String ownerId = "586678721358363";

        Persona gpt4Omni = PersonaFixture.privatePersona()

                .name("Number 1")
                .build();

        Persona gpt4Mini = PersonaFixture.privatePersona()

                .name("Number 2")
                .build();

        Persona gpt354k = PersonaFixture.privatePersona()

                .name("Number 3")
                .build();

        jpaRepository.saveAll(set(gpt4Omni, gpt4Mini, gpt354k));

        SearchPersonas query = new SearchPersonas(
                "Number 2",
                null,
                null,
                null,
                null,
                null,
                null,
                "WRITE",
                ownerId);

        // When
        SearchPersonasResult result = repository.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.results()).isNotNull().isEmpty();
    }

}
