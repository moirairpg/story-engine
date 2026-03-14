package me.moirai.storyengine.core.application.usecase.world;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.application.usecase.world.request.RemoveFavoriteWorld;
import me.moirai.storyengine.infrastructure.outbound.adapter.favorite.FavoriteRepository;

@UseCaseHandler
public class RemoveFavoriteWorldHandler extends AbstractUseCaseHandler<RemoveFavoriteWorld, Void> {

    private static final String ASSET_TYPE = "world";

    private final FavoriteRepository favoriteRepository;

    public RemoveFavoriteWorldHandler(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    @Override
    public Void execute(RemoveFavoriteWorld command) {

        favoriteRepository.deleteByPlayerIdAndAssetIdAndAssetType(
                command.getPlayerId(), command.getAssetId(), ASSET_TYPE);

        return null;
    }
}
