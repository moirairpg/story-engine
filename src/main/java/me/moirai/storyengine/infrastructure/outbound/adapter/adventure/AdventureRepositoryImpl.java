package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@Repository
public class AdventureRepositoryImpl implements AdventureRepository {

    private final AdventureJpaRepository jpaRepository;

    public AdventureRepositoryImpl(AdventureJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Adventure save(Adventure adventure) {

        return jpaRepository.save(adventure);
    }

    @Override
    public void deleteByPublicId(UUID publicId) {

        jpaRepository.deleteByPublicId(publicId);
    }

    @Override
    public void updateRememberByChannelId(String remember, String channelId) {

        jpaRepository.updateRememberByChannelId(remember, channelId);
    }

    @Override
    public void updateAuthorsNoteByChannelId(String authorsNote, String channelId) {

        jpaRepository.updateAuthorsNoteByChannelId(authorsNote, channelId);
    }

    @Override
    public void updateNudgeByChannelId(String nudge, String channelId) {

        jpaRepository.updateNudgeByChannelId(nudge, channelId);
    }

    @Override
    public void updateBumpByChannelId(String bumpContent, int bumpFrequency, String channelId) {

        jpaRepository.updateBumpByChannelId(bumpContent, bumpFrequency, channelId);
    }

    @Override
    public Optional<Adventure> findByPublicId(UUID publicId) {

        return jpaRepository.findByPublicId(publicId);
    }

    @Override
    public Optional<Adventure> findByChannelId(String channelId) {

        return jpaRepository.findByChannelId(channelId);
    }

    @Override
    public String getGameModeByChannelId(String channelId) {

        return jpaRepository.getGameModeByChannelId(channelId);
    }
}
