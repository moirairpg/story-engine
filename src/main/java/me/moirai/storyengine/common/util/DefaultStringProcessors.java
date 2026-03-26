package me.moirai.storyengine.common.util;

import static java.lang.String.format;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class DefaultStringProcessors {

    public static final String SAID = " said: ";
    public static final String PERIOD = ".";

    public static final String AUTHOR_MODE_PLACEHOLDER = "%s said: [ %s ]";
    public static final String RPG_MODE_PLACEHOLDER = "%s said:";
    public static final String CHAT_FORMAT_PLACEHOLDER = "@%s (known as %s) said: %s";
    public static final String PERSONA_NAME_PLACEHOLDER = "\\{name\\}";

    public static final String BUMP_PLACEHOLDER = "[ Bump: %s ]";
    public static final String NUDGE_PLACEHOLDER = "[ Nudge: %s ]";
    public static final String SCENE_PLACEHOLDER = "[ Current scene: %s ]";
    public static final String AUTHORS_NOTE_PLACEHOLDER = "[ Author's Note: %s ]";

    public static final String AS_NAME_PREFIX_EXPRESSION = "\\bAs %s, (\\w)";
    public static final String AS_NAME_PREFIX_LOWERCASE_EXPRESSION = "\\bas %s, (\\w)";
    public static final String USER_DISCORD_ID_EXPRESSION = "(?<=<@)\\d+(?=>)";
    public static final String CHAT_FORMAT_EXPRESSION = "^.* said:";
    public static final String TRAILING_FRAGMENT_EXPRESSION = "(?<=[.!?\\n])\"?[^.!?\\n]*(?![.!?\\n])$";
    public static final String SENTENCE_EXPRESSION = "((\\. |))(?:[ A-ZÀ-ÿa-z0-9-\"'&(),:;<>\\/\\\\]|\\.(?! ))+[\\?\\.\\!\\;'\"]$";

    private DefaultStringProcessors() {
    }

    public static UnaryOperator<String> stripAsNamePrefix(String name) {

        return input -> Pattern.compile(format(AS_NAME_PREFIX_EXPRESSION, name))
                .matcher(input)
                .replaceAll(r -> r.group(1).toUpperCase());
    }

    public static UnaryOperator<String> stripAsNamePrefixForLowercase(String name) {

        return input -> Pattern.compile(format(AS_NAME_PREFIX_LOWERCASE_EXPRESSION, name))
                .matcher(input)
                .replaceAll(r -> r.group(1));
    }

    public static UnaryOperator<String> stripTrailingFragment() {

        return input -> Pattern.compile(TRAILING_FRAGMENT_EXPRESSION, Pattern.DOTALL & Pattern.MULTILINE)
                .matcher(input)
                .replaceAll(StringUtils.EMPTY);
    }

    public static UnaryOperator<String> stripChatPrefix() {

        return input -> Pattern.compile(CHAT_FORMAT_EXPRESSION)
                .matcher(input)
                .replaceAll(StringUtils.EMPTY)
                .trim();
    }

    public static UnaryOperator<String> replacePersonaNamePlaceholderWith(String personaName) {

        return input -> Pattern.compile(PERSONA_NAME_PLACEHOLDER)
                .matcher(input)
                .replaceAll(r -> personaName);
    }

    public static UnaryOperator<String> replaceTemplateWithValue(String newValue, String template) {

        return input -> Pattern.compile(template, Pattern.MULTILINE)
                .matcher(input)
                .replaceAll(r -> newValue);
    }

    public static UnaryOperator<String> replaceTemplateWithValueIgnoreCase(String newValue, String template) {

        return input -> Pattern.compile(template, Pattern.CASE_INSENSITIVE & Pattern.MULTILINE)
                .matcher(input)
                .replaceAll(r -> newValue);
    }

    public static Function<String, String> formatChatMessage(String nickname, String username) {

        return message -> format(CHAT_FORMAT_PLACEHOLDER, username, nickname, message.trim());
    }

    public static Function<String, String> formatAuthorDirective(String nickname) {

        return message -> format(AUTHOR_MODE_PLACEHOLDER, nickname, message.trim());
    }

    public static Function<String, String> formatRpgDirective(String nickname) {

        return message -> message.replaceAll(CHAT_FORMAT_EXPRESSION, format(RPG_MODE_PLACEHOLDER, nickname));
    }

    public static Function<String, List<String>> extractDiscordIds() {

        return text -> {
            Matcher matcher = Pattern.compile(USER_DISCORD_ID_EXPRESSION).matcher(text);
            return matcher.results()
                    .map(MatchResult::group)
                    .toList();
        };
    }

    public static Function<String, String> trimParagraph() {

        return paragraph -> paragraph.trim().replaceAll(SENTENCE_EXPRESSION, PERIOD).trim();
    }

    public static Function<String, String> formatAuthorsNote() {

        return authorsNote -> format(AUTHORS_NOTE_PLACEHOLDER, authorsNote);
    }

    public static Function<String, String> formatBump() {

        return bump -> format(BUMP_PLACEHOLDER, bump);
    }

    public static Function<String, String> formatNudge() {

        return nudge -> format(NUDGE_PLACEHOLDER, nudge);
    }

    public static Function<String, String> formatScene() {

        return scene -> format(SCENE_PLACEHOLDER, scene);
    }
}