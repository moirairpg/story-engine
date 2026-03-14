package me.moirai.storyengine.core.application.usecase.world.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.set;

import org.junit.jupiter.api.Test;

public class UpdateWorldTest {

    @Test
    public void buildObject_whenAllValuesAreValid_thenCreateInstance() {

        // Given
        UpdateWorld.Builder updateWorldBuilder = UpdateWorld.builder()
                .adventureStart("SomeStart")
                .description("SomeDesc")
                .name("SomeName")
                .visibility("PUBLIC")
                .usersAllowedToReadToAdd(set("123123"))
                .usersAllowedToReadToRemove(set("123123"))
                .usersAllowedToWriteToAdd(set("123123"))
                .usersAllowedToWriteToRemove(set("123123"));

        // When
        UpdateWorld result = updateWorldBuilder.build();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isNotNull();
        assertThat(result.getAdventureStart()).isNotNull();
        assertThat(result.getDescription()).isNotNull();
        assertThat(result.getUsersAllowedToReadToAdd()).isNotNull().isNotEmpty();
        assertThat(result.getUsersAllowedToReadToRemove()).isNotNull().isNotEmpty();
        assertThat(result.getUsersAllowedToWriteToAdd()).isNotNull().isNotEmpty();
        assertThat(result.getUsersAllowedToWriteToRemove()).isNotNull().isNotEmpty();
    }

    @Test
    public void buildObject_whenModifiedListsAreNull_thenCreateInstanceWithEmptyLists() {

        // Given
        UpdateWorld.Builder updateWorldBuilder = UpdateWorld.builder()
                .adventureStart("SomeStart")
                .description("SomeDesc")
                .name("SomeName")
                .visibility("PUBLIC");

        // When
        UpdateWorld result = updateWorldBuilder.build();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isNotNull();
        assertThat(result.getAdventureStart()).isNotNull();
        assertThat(result.getDescription()).isNotNull();
        assertThat(result.getUsersAllowedToReadToAdd()).isNotNull().isEmpty();
        assertThat(result.getUsersAllowedToReadToRemove()).isNotNull().isEmpty();
        assertThat(result.getUsersAllowedToWriteToAdd()).isNotNull().isEmpty();
        assertThat(result.getUsersAllowedToWriteToRemove()).isNotNull().isEmpty();
    }
}
