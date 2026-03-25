package me.moirai.storyengine.core.port.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorld;

public class UpdateWorldTest {

    @Test
    public void buildObject_whenAllValuesAreValid_thenCreateInstance() {

        // Given
        UpdateWorld result = new UpdateWorld(
                null,
                "SomeName",
                "SomeDesc",
                "SomeStart",
                Visibility.PUBLIC,
                Set.of(123123L),
                Set.of(123123L),
                Set.of(123123L),
                Set.of(123123L));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isNotNull();
        assertThat(result.adventureStart()).isNotNull();
        assertThat(result.description()).isNotNull();
        assertThat(result.usersAllowedToReadToAdd()).isNotNull().isNotEmpty();
        assertThat(result.usersAllowedToReadToRemove()).isNotNull().isNotEmpty();
        assertThat(result.usersAllowedToWriteToAdd()).isNotNull().isNotEmpty();
        assertThat(result.usersAllowedToWriteToRemove()).isNotNull().isNotEmpty();
    }

    @Test
    public void buildObject_whenModifiedListsAreNull_thenCreateInstanceWithNullLists() {

        // Given
        UpdateWorld result = new UpdateWorld(
                null,
                "SomeName",
                "SomeDesc",
                "SomeStart",
                Visibility.PUBLIC,
                null,
                null,
                null,
                null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isNotNull();
        assertThat(result.adventureStart()).isNotNull();
        assertThat(result.description()).isNotNull();
        assertThat(result.usersAllowedToReadToAdd()).isNull();
        assertThat(result.usersAllowedToReadToRemove()).isNull();
        assertThat(result.usersAllowedToWriteToAdd()).isNull();
        assertThat(result.usersAllowedToWriteToRemove()).isNull();
    }
}
