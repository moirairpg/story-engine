package me.moirai.storyengine.core.application.usecase.adventure.request;

import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;

public class SearchAdventuresFixture {

    public static SearchAdventures.Builder writeAccess() {

        return SearchAdventures.builder()
                .name("Adventure")
                .gameMode("RPG")
                .model("GPT3_TURBO")
                .moderation("PERMISSIVE")
                .multiplayer(false)
                .operation("WRITE")
                .ownerId("1234")
                .page(1)
                .persona("1234")
                .requesterId("1234")
                .size(10)
                .sortingField("name")
                .visibility("PUBLIC")
                .world("1234");
    }

    public static SearchAdventures.Builder readAccess() {

        return SearchAdventures.builder()
                .name("Adventure")
                .gameMode("RPG")
                .model("GPT3_TURBO")
                .moderation("PERMISSIVE")
                .multiplayer(false)
                .operation("READ")
                .ownerId("1234")
                .page(1)
                .persona("1234")
                .requesterId("1234")
                .size(10)
                .sortingField("name")
                .visibility("PUBLIC")
                .world("1234");
    }
}
