package me.moirai.storyengine.core.application.helper;

import me.moirai.storyengine.common.annotation.Helper;
import me.moirai.storyengine.core.domain.adventure.AdventureRepository;

@Helper
public class AdventureHelperImpl implements AdventureHelper {

    private final AdventureRepository adventureQueryRepository;

    public AdventureHelperImpl(AdventureRepository adventureQueryRepository) {
        this.adventureQueryRepository = adventureQueryRepository;
    }

    @Override
    public String getGameModeByChannelId(String channelId) {

        return adventureQueryRepository.getGameModeByChannelId(channelId);
    }
}
