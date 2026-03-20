package me.moirai.storyengine.core.port.inbound;

import me.moirai.storyengine.common.enums.SearchView;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;

public class SearchAdventuresFixture {

    public static SearchAdventures writeAccess() {

        return new SearchAdventures(
                "Adventure",
                "1234",
                "1234",
                "1234",
                null,
                "GPT3_TURBO",
                "RPG",
                "PERMISSIVE",
                SearchView.MY_STUFF,
                null,
                null,
                1,
                10,
                "1234");
    }

    public static SearchAdventures readAccess() {

        return new SearchAdventures(
                "Adventure",
                "1234",
                "1234",
                "1234",
                null,
                "GPT3_TURBO",
                "RPG",
                "PERMISSIVE",
                SearchView.MY_STUFF,
                null,
                null,
                1,
                10,
                "1234");
    }
}
