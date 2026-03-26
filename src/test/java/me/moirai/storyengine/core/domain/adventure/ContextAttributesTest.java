package me.moirai.storyengine.core.domain.adventure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ContextAttributesTest {

    @Test
    public void contextAttributes_whenUseBuilder_thenNewInstanceIsCreated() {

        // Given
        String nudge = "Nudge";
        String authorsNote = "Author's note";
        String scene = "Scene";
        String bump = "Bump";
        int bumpFrequency = 1;

        // When
        ContextAttributes contextAttributes = new ContextAttributes(
                nudge,
                authorsNote,
                scene,
                bump,
                bumpFrequency);

        // Then
        assertThat(contextAttributes).isNotNull();
        assertThat(contextAttributes.authorsNote()).isEqualTo("Author's note");
        assertThat(contextAttributes.bump()).isEqualTo("Bump");
        assertThat(contextAttributes.nudge()).isEqualTo("Nudge");
        assertThat(contextAttributes.scene()).isEqualTo("Scene");
    }

    @Test
    public void contextAttributes_whenUpdateAuthorsNote_thenNewInstanceIsCreated() {

        // Given
        String newAuthorsNote = "New value";
        ContextAttributes contextAttributes = new ContextAttributes(
                "Nudge",
                "Author's note",
                "Scene",
                "Bump",
                1);

        // When
        ContextAttributes updatedContextAttributes = contextAttributes.updateAuthorsNote(newAuthorsNote);

        // Then
        assertThat(updatedContextAttributes).isNotNull().isNotEqualTo(contextAttributes);
        assertThat(updatedContextAttributes.authorsNote()).isEqualTo(newAuthorsNote);
        assertThat(updatedContextAttributes.bumpFrequency()).isEqualTo(1);
        assertThat(updatedContextAttributes.bump()).isEqualTo("Bump");
        assertThat(updatedContextAttributes.nudge()).isEqualTo("Nudge");
        assertThat(updatedContextAttributes.scene()).isEqualTo("Scene");
    }

    @Test
    public void contextAttributes_whenUpdateBump_thenNewInstanceIsCreated() {

        // Given
        String newBump = "New value";
        ContextAttributes contextAttributes = new ContextAttributes(
                "Nudge",
                "Author's note",
                "Scene",
                "Bump",
                1);

        // When
        ContextAttributes updatedContextAttributes = contextAttributes.updateBump(newBump);

        // Then
        assertThat(updatedContextAttributes).isNotNull().isNotEqualTo(contextAttributes);
        assertThat(updatedContextAttributes.bumpFrequency()).isEqualTo(1);
        assertThat(updatedContextAttributes.authorsNote()).isEqualTo("Author's note");
        assertThat(updatedContextAttributes.bump()).isEqualTo(newBump);
        assertThat(updatedContextAttributes.nudge()).isEqualTo("Nudge");
        assertThat(updatedContextAttributes.scene()).isEqualTo("Scene");
    }

    @Test
    public void contextAttributes_whenUpdateBumpFrequency_thenNewInstanceIsCreated() {

        // Given
        int newBumpFrequency = 3;
        ContextAttributes contextAttributes = new ContextAttributes(
                "Nudge",
                "Author's note",
                "Scene",
                "Bump",
                1);

        // When
        ContextAttributes updatedContextAttributes = contextAttributes.updateBumpFrequency(newBumpFrequency);

        // Then
        assertThat(updatedContextAttributes).isNotNull().isNotEqualTo(contextAttributes);
        assertThat(updatedContextAttributes.bumpFrequency()).isEqualTo(newBumpFrequency);
        assertThat(updatedContextAttributes.authorsNote()).isEqualTo("Author's note");
        assertThat(updatedContextAttributes.bump()).isEqualTo("Bump");
        assertThat(updatedContextAttributes.nudge()).isEqualTo("Nudge");
        assertThat(updatedContextAttributes.scene()).isEqualTo("Scene");
    }

    @Test
    public void contextAttributes_whenUpdateNudge_thenNewInstanceIsCreated() {

        // Given
        String newNudge = "New value";
        ContextAttributes contextAttributes = new ContextAttributes(
                "Nudge",
                "Author's note",
                "Scene",
                "Bump",
                1);

        // When
        ContextAttributes updatedContextAttributes = contextAttributes.updateNudge(newNudge);

        // Then
        assertThat(updatedContextAttributes).isNotNull().isNotEqualTo(contextAttributes);
        assertThat(updatedContextAttributes.bumpFrequency()).isEqualTo(1);
        assertThat(updatedContextAttributes.authorsNote()).isEqualTo("Author's note");
        assertThat(updatedContextAttributes.bump()).isEqualTo("Bump");
        assertThat(updatedContextAttributes.nudge()).isEqualTo(newNudge);
        assertThat(updatedContextAttributes.scene()).isEqualTo("Scene");
    }

    @Test
    public void contextAttributes_whenUpdateScene_thenNewInstanceIsCreated() {

        // Given
        String newScene = "New value";
        ContextAttributes contextAttributes = new ContextAttributes(
                "Nudge",
                "Author's note",
                "Scene",
                "Bump",
                1);

        // When
        ContextAttributes updatedContextAttributes = contextAttributes.updateScene(newScene);

        // Then
        assertThat(updatedContextAttributes).isNotNull().isNotEqualTo(contextAttributes);
        assertThat(updatedContextAttributes.bumpFrequency()).isEqualTo(1);
        assertThat(updatedContextAttributes.authorsNote()).isEqualTo("Author's note");
        assertThat(updatedContextAttributes.bump()).isEqualTo("Bump");
        assertThat(updatedContextAttributes.nudge()).isEqualTo("Nudge");
        assertThat(updatedContextAttributes.scene()).isEqualTo(newScene);
    }
}
