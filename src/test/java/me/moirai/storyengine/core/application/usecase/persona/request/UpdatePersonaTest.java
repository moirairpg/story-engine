package me.moirai.storyengine.core.application.usecase.persona.request;

import me.moirai.storyengine.core.port.inbound.persona.UpdatePersona;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.set;

import java.util.Set;

import org.junit.jupiter.api.Test;

public class UpdatePersonaTest {

    @Test
    public void buildObject_whenAllValuesAreSupplied_thenBuildObject() {

        // Given
        String name = "name";
        String personality = "personality";
        String visibility = "PRIVATE";
        Set<String> usersAllowedToReadToAdd = set("123123", "123123");
        Set<String> usersAllowedToWriteToAdd = set("123123", "123123");
        Set<String> usersAllowedToReadToRemove = set("123123", "123123");
        Set<String> usersAllowedToWriteToRemove = set("123123", "123123");

        UpdatePersona.Builder updatePersonaBuilder = UpdatePersona.builder()
                .name(name)
                .personality(personality)
                .visibility(visibility)
                .usersAllowedToReadToAdd(usersAllowedToReadToAdd)
                .usersAllowedToWriteToAdd(usersAllowedToWriteToAdd)
                .usersAllowedToReadToRemove(usersAllowedToReadToRemove)
                .usersAllowedToWriteToRemove(usersAllowedToWriteToRemove);

        // When
        UpdatePersona updatePersona = updatePersonaBuilder.build();

        // Then
        assertThat(updatePersona).isNotNull();
        assertThat(updatePersona.getName()).isNotNull().isNotEmpty().isEqualTo(name);
        assertThat(updatePersona.getPersonality()).isNotNull().isNotEmpty().isEqualTo(personality);
        assertThat(updatePersona.getVisibility()).isNotNull().isNotEmpty().isEqualTo(visibility);
        assertThat(updatePersona.getUsersAllowedToReadToAdd()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToReadToAdd);
        assertThat(updatePersona.getUsersAllowedToWriteToAdd()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToWriteToAdd);
        assertThat(updatePersona.getUsersAllowedToReadToRemove()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToReadToRemove);
        assertThat(updatePersona.getUsersAllowedToWriteToRemove()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToWriteToRemove);
    }

    @Test
    public void buildObject_whenWriterUsersToAddNotSupplied_thenBuildObjectWithEmptyList() {

        // Given
        String name = "name";
        String personality = "personality";
        String visibility = "PRIVATE";
        Set<String> usersAllowedToReadToAdd = set("123123", "123123");
        Set<String> usersAllowedToWriteToAdd = null;
        Set<String> usersAllowedToReadToRemove = set("123123", "123123");
        Set<String> usersAllowedToWriteToRemove = set("123123", "123123");

        UpdatePersona.Builder updatePersonaBuilder = UpdatePersona.builder()
                .name(name)
                .personality(personality)
                .visibility(visibility)
                .usersAllowedToReadToAdd(usersAllowedToReadToAdd)
                .usersAllowedToWriteToAdd(usersAllowedToWriteToAdd)
                .usersAllowedToReadToRemove(usersAllowedToReadToRemove)
                .usersAllowedToWriteToRemove(usersAllowedToWriteToRemove);

        // When
        UpdatePersona updatePersona = updatePersonaBuilder.build();

        // Then
        assertThat(updatePersona).isNotNull();
        assertThat(updatePersona.getName()).isNotNull().isNotEmpty().isEqualTo(name);
        assertThat(updatePersona.getPersonality()).isNotNull().isNotEmpty().isEqualTo(personality);
        assertThat(updatePersona.getVisibility()).isNotNull().isNotEmpty().isEqualTo(visibility);
        assertThat(updatePersona.getUsersAllowedToReadToAdd()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToReadToAdd);
        assertThat(updatePersona.getUsersAllowedToWriteToAdd()).isNotNull().isEmpty();
        assertThat(updatePersona.getUsersAllowedToReadToRemove()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToReadToRemove);
        assertThat(updatePersona.getUsersAllowedToWriteToRemove()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToWriteToRemove);
    }

    @Test
    public void buildObject_whenReaderUsersToAddNotSupplied_thenBuildObjectWithEmptyList() {

        // Given
        String name = "name";
        String personality = "personality";
        String visibility = "PRIVATE";
        Set<String> usersAllowedToReadToAdd = null;
        Set<String> usersAllowedToWriteToAdd = set("123123", "123123");
        Set<String> usersAllowedToReadToRemove = set("123123", "123123");
        Set<String> usersAllowedToWriteToRemove = set("123123", "123123");

        UpdatePersona.Builder updatePersonaBuilder = UpdatePersona.builder()
                .name(name)
                .personality(personality)
                .visibility(visibility)
                .usersAllowedToReadToAdd(usersAllowedToReadToAdd)
                .usersAllowedToWriteToAdd(usersAllowedToWriteToAdd)
                .usersAllowedToReadToRemove(usersAllowedToReadToRemove)
                .usersAllowedToWriteToRemove(usersAllowedToWriteToRemove);

        // When
        UpdatePersona updatePersona = updatePersonaBuilder.build();

        // Then
        assertThat(updatePersona).isNotNull();
        assertThat(updatePersona.getName()).isNotNull().isNotEmpty().isEqualTo(name);
        assertThat(updatePersona.getPersonality()).isNotNull().isNotEmpty().isEqualTo(personality);
        assertThat(updatePersona.getVisibility()).isNotNull().isNotEmpty().isEqualTo(visibility);
        assertThat(updatePersona.getUsersAllowedToReadToAdd()).isNotNull().isEmpty();
        assertThat(updatePersona.getUsersAllowedToWriteToAdd()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToWriteToAdd);
        assertThat(updatePersona.getUsersAllowedToReadToRemove()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToReadToRemove);
        assertThat(updatePersona.getUsersAllowedToWriteToRemove()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToWriteToRemove);
    }

    @Test
    public void buildObject_whenReaderUsersToRemoveNotSupplied_thenBuildObjectWithEmptyList() {

        // Given
        String name = "name";
        String personality = "personality";
        String visibility = "PRIVATE";
        Set<String> usersAllowedToReadToAdd = set("123123", "123123");
        Set<String> usersAllowedToWriteToAdd = set("123123", "123123");
        Set<String> usersAllowedToReadToRemove = null;
        Set<String> usersAllowedToWriteToRemove = set("123123", "123123");

        UpdatePersona.Builder updatePersonaBuilder = UpdatePersona.builder()
                .name(name)
                .personality(personality)
                .visibility(visibility)
                .usersAllowedToReadToAdd(usersAllowedToReadToAdd)
                .usersAllowedToWriteToAdd(usersAllowedToWriteToAdd)
                .usersAllowedToReadToRemove(usersAllowedToReadToRemove)
                .usersAllowedToWriteToRemove(usersAllowedToWriteToRemove);

        // When
        UpdatePersona updatePersona = updatePersonaBuilder.build();

        // Then
        assertThat(updatePersona).isNotNull();
        assertThat(updatePersona.getName()).isNotNull().isNotEmpty().isEqualTo(name);
        assertThat(updatePersona.getPersonality()).isNotNull().isNotEmpty().isEqualTo(personality);
        assertThat(updatePersona.getVisibility()).isNotNull().isNotEmpty().isEqualTo(visibility);
        assertThat(updatePersona.getUsersAllowedToReadToAdd()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToWriteToAdd);
        assertThat(updatePersona.getUsersAllowedToWriteToAdd()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToWriteToAdd);
        assertThat(updatePersona.getUsersAllowedToReadToRemove()).isNotNull().isEmpty();
        assertThat(updatePersona.getUsersAllowedToWriteToRemove()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToWriteToRemove);
    }

    @Test
    public void buildObject_whenWriterUsersToRemoveNotSupplied_thenBuildObjectWithEmptyList() {

        // Given
        String name = "name";
        String personality = "personality";
        String visibility = "PRIVATE";
        Set<String> usersAllowedToReadToAdd = set("123123", "123123");
        Set<String> usersAllowedToWriteToAdd = set("123123", "123123");
        Set<String> usersAllowedToReadToRemove = set("123123", "123123");
        Set<String> usersAllowedToWriteToRemove = null;

        UpdatePersona.Builder updatePersonaBuilder = UpdatePersona.builder()
                .name(name)
                .personality(personality)
                .visibility(visibility)
                .usersAllowedToReadToAdd(usersAllowedToReadToAdd)
                .usersAllowedToWriteToAdd(usersAllowedToWriteToAdd)
                .usersAllowedToReadToRemove(usersAllowedToReadToRemove)
                .usersAllowedToWriteToRemove(usersAllowedToWriteToRemove);

        // When
        UpdatePersona updatePersona = updatePersonaBuilder.build();

        // Then
        assertThat(updatePersona).isNotNull();
        assertThat(updatePersona.getName()).isNotNull().isNotEmpty().isEqualTo(name);
        assertThat(updatePersona.getPersonality()).isNotNull().isNotEmpty().isEqualTo(personality);
        assertThat(updatePersona.getVisibility()).isNotNull().isNotEmpty().isEqualTo(visibility);
        assertThat(updatePersona.getUsersAllowedToReadToAdd()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToWriteToAdd);
        assertThat(updatePersona.getUsersAllowedToWriteToAdd()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToWriteToAdd);
        assertThat(updatePersona.getUsersAllowedToReadToRemove()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToReadToRemove);
        assertThat(updatePersona.getUsersAllowedToWriteToRemove()).isNotNull().isEmpty();
    }
}
