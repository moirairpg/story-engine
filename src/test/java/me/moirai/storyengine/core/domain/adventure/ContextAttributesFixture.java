package me.moirai.storyengine.core.domain.adventure;

public class ContextAttributesFixture {

    public static ContextAttributes.Builder sample() {

        return ContextAttributes.builder()
                .authorsNote("Author's note")
                .nudge("Nudge")
                .remember("Remember")
                .bump("Bump")
                .bumpFrequency(1);
    }
}
