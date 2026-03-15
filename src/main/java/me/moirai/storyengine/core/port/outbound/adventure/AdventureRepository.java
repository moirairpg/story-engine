package me.moirai.storyengine.core.port.outbound.adventure;

import java.util.Optional;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntriesResult;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventuresResult;

public interface AdventureRepository {

    Adventure save(Adventure adventure);

    void deleteById(Long id);

    void deleteByPublicId(String publicId);

    void updateRememberByChannelId(String remember, String channelId);

    void updateAuthorsNoteByChannelId(String authorsNote, String channelId);

    void updateNudgeByChannelId(String nudge, String channelId);

    void updateBumpByChannelId(String bumpContent, int bumpFrequency, String channelId);

    Optional<Adventure> findById(Long id);

    Optional<Adventure> findByPublicId(String publicId);

    SearchAdventuresResult search(SearchAdventures request);

    SearchAdventureLorebookEntriesResult searchLorebookEntries(SearchAdventureLorebookEntries query);

    Optional<Adventure> findByChannelId(String channelId);

    String getGameModeByChannelId(String channelId);
}
