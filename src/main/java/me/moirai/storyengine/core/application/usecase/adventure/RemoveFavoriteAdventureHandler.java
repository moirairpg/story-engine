package me.moirai.storyengine.core.application.usecase.adventure;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.application.usecase.adventure.request.RemoveFavoriteAdventure;
import me.moirai.storyengine.infrastructure.outbound.persistence.FavoriteRepository;

@UseCaseHandler
public class RemoveFavoriteAdventureHandler extends AbstractUseCaseHandler<RemoveFavoriteAdventure, Void> {

    private static final String ASSET_TYPE = "adventure";

    private final FavoriteRepository favoriteRepository;

    public RemoveFavoriteAdventureHandler(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    @Override
    public Void execute(RemoveFavoriteAdventure command) {

        favoriteRepository.deleteByPlayerIdAndAssetIdAndAssetType(
                command.getPlayerId(), command.getAssetId(), ASSET_TYPE);

        return null;
    }
}
