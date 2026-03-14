package me.moirai.storyengine.core.domain.adventure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ContextAttributesTest {

    @Test
    public void contextAttributes_whenUseBuilder_thenNewInstanceIsCreated() {

        // Given
        ContextAttributes.Builder builder = ContextAttributes.builder()
                .authorsNote("Author's note")
                .bump("Bump")
                .nudge("Nudge")
                .remember("Remember")
                .bumpFrequency(1);

        // When
        ContextAttributes contextAttributes = builder.build();

        // Then
        assertThat(contextAttributes).isNotNull();
        assertThat(contextAttributes.getAuthorsNote()).isEqualTo("Author's note");
        assertThat(contextAttributes.getBump()).isEqualTo("Bump");
        assertThat(contextAttributes.getNudge()).isEqualTo("Nudge");
        assertThat(contextAttributes.getRemember()).isEqualTo("Remember");
    }

    @Test
    public void contextAttributes_whenUpdateAuthorsNote_thenNewInstanceIsCreated() {

        // Given
        String newAuthorsNote = "New value";
        ContextAttributes contextAttributes = ContextAttributes.builder()
                .authorsNote("Author's note")
                .bump("Bump")
                .nudge("Nudge")
                .remember("Remember")
                .bumpFrequency(1)
                .build();

        // When
        ContextAttributes updatedContextAttributes = contextAttributes.updateAuthorsNote(newAuthorsNote);

        // Then
        assertThat(updatedContextAttributes).isNotNull().isNotEqualTo(contextAttributes);
        assertThat(updatedContextAttributes.getAuthorsNote()).isEqualTo(newAuthorsNote);
        assertThat(updatedContextAttributes.getBumpFrequency()).isEqualTo(1);
        assertThat(updatedContextAttributes.getBump()).isEqualTo("Bump");
        assertThat(updatedContextAttributes.getNudge()).isEqualTo("Nudge");
        assertThat(updatedContextAttributes.getRemember()).isEqualTo("Remember");
    }

    @Test
    public void contextAttributes_whenUpdateBump_thenNewInstanceIsCreated() {

        // Given
        String newBump = "New value";
        ContextAttributes contextAttributes = ContextAttributes.builder()
                .authorsNote("Author's note")
                .bump("Bump")
                .nudge("Nudge")
                .remember("Remember")
                .bumpFrequency(1)
                .build();

        // When
        ContextAttributes updatedContextAttributes = contextAttributes.updateBump(newBump);

        // Then
        assertThat(updatedContextAttributes).isNotNull().isNotEqualTo(contextAttributes);
        assertThat(updatedContextAttributes.getBumpFrequency()).isEqualTo(1);
        assertThat(updatedContextAttributes.getAuthorsNote()).isEqualTo("Author's note");
        assertThat(updatedContextAttributes.getBump()).isEqualTo(newBump);
        assertThat(updatedContextAttributes.getNudge()).isEqualTo("Nudge");
        assertThat(updatedContextAttributes.getRemember()).isEqualTo("Remember");
    }

    @Test
    public void contextAttributes_whenUpdateBumpFrequency_thenNewInstanceIsCreated() {

        // Given
        int newBumpFrequency = 3;
        ContextAttributes contextAttributes = ContextAttributes.builder()
                .authorsNote("Author's note")
                .nudge("Nudge")
                .remember("Remember")
                .bump("Bump")
                .bumpFrequency(1)
                .build();

        // When
        ContextAttributes updatedContextAttributes = contextAttributes.updateBumpFrequency(newBumpFrequency);

        // Then
        assertThat(updatedContextAttributes).isNotNull().isNotEqualTo(contextAttributes);
        assertThat(updatedContextAttributes.getBumpFrequency()).isEqualTo(newBumpFrequency);
        assertThat(updatedContextAttributes.getAuthorsNote()).isEqualTo("Author's note");
        assertThat(updatedContextAttributes.getBump()).isEqualTo("Bump");
        assertThat(updatedContextAttributes.getNudge()).isEqualTo("Nudge");
        assertThat(updatedContextAttributes.getRemember()).isEqualTo("Remember");
    }

    @Test
    public void contextAttributes_whenUpdateNudge_thenNewInstanceIsCreated() {

        // Given
        String newNudge = "New value";
        ContextAttributes contextAttributes = ContextAttributes.builder()
                .authorsNote("Author's note")
                .bump("Bump")
                .nudge("Nudge")
                .remember("Remember")
                .bumpFrequency(1)
                .build();

        // When
        ContextAttributes updatedContextAttributes = contextAttributes.updateNudge(newNudge);

        // Then
        assertThat(updatedContextAttributes).isNotNull().isNotEqualTo(contextAttributes);
        assertThat(updatedContextAttributes.getBumpFrequency()).isEqualTo(1);
        assertThat(updatedContextAttributes.getAuthorsNote()).isEqualTo("Author's note");
        assertThat(updatedContextAttributes.getBump()).isEqualTo("Bump");
        assertThat(updatedContextAttributes.getNudge()).isEqualTo(newNudge);
        assertThat(updatedContextAttributes.getRemember()).isEqualTo("Remember");
    }

    @Test
    public void contextAttributes_whenUpdateRemember_thenNewInstanceIsCreated() {

        // Given
        String newRemember = "New value";
        ContextAttributes contextAttributes = ContextAttributes.builder()
                .authorsNote("Author's note")
                .bump("Bump")
                .nudge("Nudge")
                .remember("Remember")
                .bumpFrequency(1)
                .build();

        // When
        ContextAttributes updatedContextAttributes = contextAttributes.updateRemember(newRemember);

        // Then
        assertThat(updatedContextAttributes).isNotNull().isNotEqualTo(contextAttributes);
        assertThat(updatedContextAttributes.getBumpFrequency()).isEqualTo(1);
        assertThat(updatedContextAttributes.getAuthorsNote()).isEqualTo("Author's note");
        assertThat(updatedContextAttributes.getBump()).isEqualTo("Bump");
        assertThat(updatedContextAttributes.getNudge()).isEqualTo("Nudge");
        assertThat(updatedContextAttributes.getRemember()).isEqualTo(newRemember);
    }
}
