package me.moirai.storyengine.core.application.usecase.persona;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.application.usecase.persona.request.RemoveFavoritePersona;
import me.moirai.storyengine.infrastructure.outbound.adapter.favorite.FavoriteRepository;

@UseCaseHandler
public class RemoveFavoritePersonaHandler extends AbstractUseCaseHandler<RemoveFavoritePersona, Void> {

    private static final String ASSET_TYPE = "persona";

    private final FavoriteRepository favoriteRepository;

    public RemoveFavoritePersonaHandler(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    @Override
    public Void execute(RemoveFavoritePersona command) {

        favoriteRepository.deleteByPlayerIdAndAssetIdAndAssetType(
                command.getPlayerId(), command.getAssetId(), ASSET_TYPE);

        return null;
    }
}
