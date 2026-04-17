package me.moirai.storyengine.core.port.inbound;

import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryFixture;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntry;

public class UpdateAdventureLorebookEntryFixture {

    public static UpdateAdventureLorebookEntry sampleLorebookEntry() {

        return new UpdateAdventureLorebookEntry(
                AdventureLorebookEntryFixture.PUBLIC_ID,
                AdventureFixture.PUBLIC_ID,
                "Volin Habar",
                "Volin Habar is a warrior that fights with a sword.",
                null);
    }

    public static UpdateAdventureLorebookEntry samplePlayerCharacterLorebookEntry() {

        return new UpdateAdventureLorebookEntry(
                AdventureLorebookEntryFixture.PUBLIC_ID,
                AdventureFixture.PUBLIC_ID,
                "Volin Habar",
                "Volin Habar is a warrior that fights with a sword.",
                "2423423423423");
    }
}
