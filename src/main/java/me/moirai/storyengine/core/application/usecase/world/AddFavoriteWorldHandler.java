package me.moirai.storyengine.core.application.usecase.world;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.world.AddFavoriteWorld;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldRepository;
import me.moirai.storyengine.infrastructure.outbound.adapter.favorite.FavoriteEntity;
import me.moirai.storyengine.infrastructure.outbound.adapter.favorite.FavoriteRepository;

@UseCaseHandler
public class AddFavoriteWorldHandler extends AbstractUseCaseHandler<AddFavoriteWorld, Void> {

    private static final String ASSET_TYPE = "world";
    private static final String USER_NO_PERMISSION_IN_PERSONA = "User does not have permission to view the persona";

    private final WorldRepository worldRepository;
    private final FavoriteRepository favoriteRepository;

    public AddFavoriteWorldHandler(
            WorldRepository worldRepository,
            FavoriteRepository favoriteRepository) {

        this.worldRepository = worldRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @Override
    public Void execute(AddFavoriteWorld command) {

        World world = worldRepository.findById(command.getAssetId())
                .orElseThrow(() -> new AssetNotFoundException("The world to be favorited could not be found"));

        if (!world.canUserRead(command.getPlayerId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_PERSONA);
        }

        favoriteRepository.save(FavoriteEntity.builder()
                .assetType(ASSET_TYPE)
                .assetId(command.getAssetId())
                .playerId(command.getPlayerId())
                .build());

        return null;
    }
}
