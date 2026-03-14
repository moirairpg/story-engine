package me.moirai.storyengine.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

public class DefaultStringProcessorsTest {

    @Test
    public void processor_whenPrefixWithNameUppercase_thenStripPrefix() {

        // Given
        String name = "John";
        String input = "As John, how are you doing today?";

        // When
        String output = DefaultStringProcessors.stripAsNamePrefix(name).apply(input);

        // Then
        assertThat(output).isEqualTo("How are you doing today?");
    }

    @Test
    public void processor_whenPrefixWithNameLowercase_thenStripPrefix() {

        // Given
        String name = "John";
        String input = "as John, how are you doing today?";

        // When
        String output = DefaultStringProcessors.stripAsNamePrefixForLowercase(name).apply(input);

        // Then
        assertThat(output).isEqualTo("how are you doing today?");
    }

    @Test
    public void processor_whenIncompleteSentence_thenStripIncompletePartOut() {

        // Given
        String input = "This is a test. This sentence is complete. But this last one should be cut";

        // When
        String output = DefaultStringProcessors.stripTrailingFragment().apply(input);

        // Then
        assertThat(output).isEqualTo("This is a test. This sentence is complete.");
    }

    @Test
    public void processor_whenChatFormatDetected_thenNormalizeToFlatString() {

        // Given
        String input = "Marcus said: This is a test. This sentence is complete.";

        // When
        String output = DefaultStringProcessors.stripChatPrefix().apply(input);

        // Then
        assertThat(output).isEqualTo("This is a test. This sentence is complete.");
    }

    @Test
    public void processor_whenChatFormatDetectedWithCompositeName_thenNormalizeToFlatString() {

        // Given
        String input = "Marcus John said: This is a test. This sentence is complete.";

        // When
        String output = DefaultStringProcessors.stripChatPrefix().apply(input);

        // Then
        assertThat(output).isEqualTo("This is a test. This sentence is complete.");
    }

    @Test
    public void processor_whenNameToBeReplacedDetected_thenReplaceWithInput() {

        // Given
        String nameToUse = "John";
        String nameToReplace = "Geraldo";
        String input = "as Geraldo, how are you doing today?";

        // When
        String output = DefaultStringProcessors.replaceTemplateWithValue(nameToUse, nameToReplace).apply(input);

        // Then
        assertThat(output).isEqualTo("as John, how are you doing today?");
    }

    @Test
    public void processor_whenNamePlaceholderDetected_thenReplaceWithInput() {

        // Given
        String nameToUse = "John";
        String input = "as {name}, how are you doing today?";

        // When
        String output = DefaultStringProcessors.replacePersonaNamePlaceholderWith(nameToUse).apply(input);

        // Then
        assertThat(output).isEqualTo("as John, how are you doing today?");
    }

    @Test
    public void processor_whenChatMessageFormatRequired_thenFormatInput() {

        // Given
        String nickname = "John";
        String username = "JohnSmith";
        String input = "how are you doing today?";

        // When
        String output = DefaultStringProcessors.formatChatMessage(nickname, username).apply(input);

        // Then
        assertThat(output).isEqualTo("@JohnSmith (known as John) said: how are you doing today?");
    }

    @Test
    public void processor_whenMessageWithDiscordMentionsProvided_thenExtractDiscordIds() {

        // Given
        String input = "This is <@4324234>, and <@4234234235> is also here.";

        // When
        List<String> output = DefaultStringProcessors.extractDiscordIds().apply(input);

        // Then
        assertThat(output).isNotNull()
                .isNotEmpty()
                .hasSize(2);

        assertThat(output).element(0).isEqualTo("4324234");
        assertThat(output).element(1).isEqualTo("4234234235");
    }

    @Test
    public void processor_whenAuthorFormatRequired_thenFormatInput() {

        // Given
        String nameToUse = "John";
        String input = "how are you doing today?";

        // When
        String output = DefaultStringProcessors.formatAuthorDirective(nameToUse).apply(input);

        // Then
        assertThat(output).isEqualTo("John said: [ how are you doing today? ]");
    }

    @Test
    public void processor_whenParagraphTooLong_thenTrimLastPiece() {

        // Given
        String paragraph = "It was a question of which of the two she preferred. On the one hand, the choice seemed simple. The more expensive one with a brand name would be the choice of most. It was the easy choice. The safe choice. But she wasn't sure she actually preferred it.";
        String expectedResult = "It was a question of which of the two she preferred. On the one hand, the choice seemed simple. The more expensive one with a brand name would be the choice of most. It was the easy choice. The safe choice.";

        // When
        String output = DefaultStringProcessors.trimParagraph().apply(paragraph);

        // Then
        assertThat(output).isNotBlank().isEqualTo(expectedResult);
    }

    @Test
    public void processor_whenChatFormatted_thenFormatToRpgDirective() {

        // Given
        String nickname = "Gabler the Great";
        String messageContent = "@marcus.jones (known as Marcus) said: This is a message.";
        String expectedFormattedContent = "Gabler the Great said: This is a message.";

        // When
        String result = DefaultStringProcessors.formatRpgDirective(nickname).apply(messageContent);

        // Then
        assertThat(result).isEqualTo(expectedFormattedContent);
    }

    @Test
    public void processor_whenBumpReceived_thenReturnFormatted() {

        // Given
        String bump = "This is a bump";
        String expectedBump = "[ Bump: This is a bump ]";

        // When
        String result = DefaultStringProcessors.formatBump().apply(bump);

        // Then
        assertThat(result).isEqualTo(expectedBump);
    }

    @Test
    public void processor_whenNudgeReceived_thenReturnFormatted() {

        // Given
        String nudge = "This is a nudge";
        String expectedNudge = "[ Nudge: This is a nudge ]";

        // When
        String result = DefaultStringProcessors.formatNudge().apply(nudge);

        // Then
        assertThat(result).isEqualTo(expectedNudge);
    }

    @Test
    public void processor_whenRememberReceived_thenReturnFormatted() {

        // Given
        String remember = "This is a remember";
        String expectedRemember = "[ Important to remember: This is a remember ]";

        // When
        String result = DefaultStringProcessors.formatRemember().apply(remember);

        // Then
        assertThat(result).isEqualTo(expectedRemember);
    }

    @Test
    public void processor_whenAuthorsNoteReceived_thenReturnFormatted() {

        // Given
        String authorsNote = "This is a authorsNote";
        String expectedAuthorsNote = "[ Author's Note: This is a authorsNote ]";

        // When
        String result = DefaultStringProcessors.formatAuthorsNote().apply(authorsNote);

        // Then
        assertThat(result).isEqualTo(expectedAuthorsNote);
    }
}
