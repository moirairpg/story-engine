package me.moirai.storyengine.core.domain.adventure;

import java.util.Optional;

import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventuresResult;

public interface AdventureRepository {

    Adventure save(Adventure adventure);

    void deleteById(String id);

    void updateRememberByChannelId(String remember, String channelId);

    void updateAuthorsNoteByChannelId(String authorsNote, String channelId);

    void updateNudgeByChannelId(String nudge, String channelId);

    void updateBumpByChannelId(String bumpContent, int bumpFrequency, String channelId);

    Optional<Adventure> findById(String id);

    SearchAdventuresResult search(SearchAdventures request);

    Optional<Adventure> findByChannelId(String channelId);

    String getGameModeByChannelId(String channelId);
}
