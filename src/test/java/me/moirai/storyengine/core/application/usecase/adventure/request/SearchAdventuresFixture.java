package me.moirai.storyengine.core.application.usecase.adventure.request;

import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;

public class SearchAdventuresFixture {

    public static SearchAdventures writeAccess() {

        return new SearchAdventures(
                "Adventure",
                "1234",
                "1234",
                "1234",
                false,
                1,
                10,
                "GPT3_TURBO",
                "RPG",
                "PERMISSIVE",
                "name",
                "ASC",
                "PUBLIC",
                "WRITE",
                "1234");
    }

    public static SearchAdventures readAccess() {

        return new SearchAdventures(
                "Adventure",
                "1234",
                "1234",
                "1234",
                false,
                1,
                10,
                "GPT3_TURBO",
                "RPG",
                "PERMISSIVE",
                "name",
                "ASC",
                "PUBLIC",
                "READ",
                "1234");
    }
}
