package me.moirai.storyengine.core.port.inbound;

import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntry;

public class CreateAdventureLorebookEntryFixture {

    public static CreateAdventureLorebookEntry sampleLorebookEntry() {

        return new CreateAdventureLorebookEntry(
                AdventureFixture.PUBLIC_ID,
                "Volin Habar",
                "Volin Habar is a warrior that fights with a sword.",
                null);
    }

    public static CreateAdventureLorebookEntry samplePlayerCharacterLorebookEntry() {

        return new CreateAdventureLorebookEntry(
                AdventureFixture.PUBLIC_ID,
                "Volin Habar",
                "Volin Habar is a warrior that fights with a sword.",
                "2423423423423");
    }
}
