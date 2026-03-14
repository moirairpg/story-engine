package me.moirai.storyengine.core.application.usecase.persona.request;

import me.moirai.storyengine.core.port.inbound.persona.CreatePersona;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.set;

import java.util.Set;

import org.junit.jupiter.api.Test;

public class CreatePersonaTest {

    @Test
    public void buildObject_whenAllValuesAreSupplied_thenBuildObject() {

        // Given
        String name = "name";
        String personality = "personality";
        String visibility = "PRIVATE";
        String requesterId = "123123";
        Set<String> usersAllowedToRead = set("123123", "123123");
        Set<String> usersAllowedToWrite = set("123123", "123123");

        CreatePersona.Builder createPersonaBuilder = CreatePersona.builder()
                .name(name)
                .personality(personality)
                .visibility(visibility)
                .requesterId(requesterId)
                .usersAllowedToWrite(usersAllowedToWrite)
                .usersAllowedToRead(usersAllowedToRead);

        // When
        CreatePersona createPersona = createPersonaBuilder.build();

        // Then
        assertThat(createPersona).isNotNull();
        assertThat(createPersona.getName()).isNotNull().isNotEmpty().isEqualTo(name);
        assertThat(createPersona.getPersonality()).isNotNull().isNotEmpty().isEqualTo(personality);
        assertThat(createPersona.getVisibility()).isNotNull().isNotEmpty().isEqualTo(visibility);
        assertThat(createPersona.getRequesterDiscordId()).isNotNull().isNotEmpty().isEqualTo(requesterId);
        assertThat(createPersona.getUsersAllowedToRead()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToRead);
        assertThat(createPersona.getUsersAllowedToWrite()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToWrite);
    }

    @Test
    public void buildObject_whenWriterUsersNotSupplied_thenBuildObjectWithEmptyList() {

        // Given
        String name = "name";
        String personality = "personality";
        String visibility = "PRIVATE";
        String requesterId = "123123";
        Set<String> usersAllowedToRead = set("123123", "123123");
        Set<String> usersAllowedToWrite = null;

        CreatePersona.Builder createPersonaBuilder = CreatePersona.builder()
                .name(name)
                .personality(personality)
                .visibility(visibility)
                .requesterId(requesterId)
                .usersAllowedToWrite(usersAllowedToWrite)
                .usersAllowedToRead(usersAllowedToRead);

        // When
        CreatePersona createPersona = createPersonaBuilder.build();

        // Then
        assertThat(createPersona).isNotNull();
        assertThat(createPersona.getName()).isNotNull().isNotEmpty().isEqualTo(name);
        assertThat(createPersona.getPersonality()).isNotNull().isNotEmpty().isEqualTo(personality);
        assertThat(createPersona.getVisibility()).isNotNull().isNotEmpty().isEqualTo(visibility);
        assertThat(createPersona.getRequesterDiscordId()).isNotNull().isNotEmpty().isEqualTo(requesterId);
        assertThat(createPersona.getUsersAllowedToRead()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToRead);
        assertThat(createPersona.getUsersAllowedToWrite()).isNotNull().isEmpty();
    }

    @Test
    public void buildObject_whenReaderUsersNotSupplied_thenBuildObjectWithEmptyList() {

        // Given
        String name = "name";
        String personality = "personality";
        String visibility = "PRIVATE";
        String requesterId = "123123";
        Set<String> usersAllowedToRead = null;
        Set<String> usersAllowedToWrite = set("123123", "123123");

        CreatePersona.Builder createPersonaBuilder = CreatePersona.builder()
                .name(name)
                .personality(personality)
                .visibility(visibility)
                .requesterId(requesterId)
                .usersAllowedToWrite(usersAllowedToWrite)
                .usersAllowedToRead(usersAllowedToRead);

        // When
        CreatePersona createPersona = createPersonaBuilder.build();

        // Then
        assertThat(createPersona).isNotNull();
        assertThat(createPersona.getName()).isNotNull().isNotEmpty().isEqualTo(name);
        assertThat(createPersona.getPersonality()).isNotNull().isNotEmpty().isEqualTo(personality);
        assertThat(createPersona.getVisibility()).isNotNull().isNotEmpty().isEqualTo(visibility);
        assertThat(createPersona.getRequesterDiscordId()).isNotNull().isNotEmpty().isEqualTo(requesterId);
        assertThat(createPersona.getUsersAllowedToRead()).isNotNull().isEmpty();
        assertThat(createPersona.getUsersAllowedToWrite()).isNotNull().isNotEmpty().hasSameElementsAs(usersAllowedToWrite);
    }
}
