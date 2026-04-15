package me.moirai.storyengine.core.port.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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
                null,
                null,
                Visibility.PUBLIC,
                Set.of(),
                List.of(),
                List.of(),
                List.of());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isNotNull();
        assertThat(result.adventureStart()).isNotNull();
        assertThat(result.description()).isNotNull();
        assertThat(result.permissions()).isNotNull().isEmpty();
    }

    @Test
    public void buildObject_whenPermissionsIsNull_thenSetIsEmpty() {

        // Given
        UpdateWorld result = new UpdateWorld(
                null,
                "SomeName",
                "SomeDesc",
                "SomeStart",
                null,
                null,
                Visibility.PUBLIC,
                null,
                List.of(),
                List.of(),
                List.of());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isNotNull();
        assertThat(result.adventureStart()).isNotNull();
        assertThat(result.description()).isNotNull();
        assertThat(result.permissions()).isEmpty();
    }
}
