package me.moirai.storyengine.core.port.outbound.adventure;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.domain.adventure.Adventure;

public interface AdventureRepository {

    Adventure save(Adventure adventure);

    void deleteByPublicId(UUID publicId);

    void updateRememberByChannelId(String remember, String channelId);

    void updateAuthorsNoteByChannelId(String authorsNote, String channelId);

    void updateNudgeByChannelId(String nudge, String channelId);

    void updateBumpByChannelId(String bumpContent, int bumpFrequency, String channelId);

    Optional<Adventure> findByPublicId(UUID publicId);

    Optional<Adventure> findByChannelId(String channelId);

    String getGameModeByChannelId(String channelId);
}
