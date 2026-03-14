package me.moirai.storyengine.core.application.usecase.adventure;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.adventure.AddFavoriteAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.infrastructure.outbound.adapter.favorite.FavoriteEntity;
import me.moirai.storyengine.infrastructure.outbound.adapter.favorite.FavoriteRepository;

@UseCaseHandler
public class AddFavoriteAdventureHandler extends AbstractUseCaseHandler<AddFavoriteAdventure, Void> {

    private static final String USER_NO_PERMISSION = "User does not have permission to view this adventure";
    private static final String ADVENTURE_NOT_BE_FOUND = "The adventure to be favorited could not be found";

    private static final String ASSET_TYPE = "adventure";

    private final AdventureRepository adventureQueryRepository;
    private final FavoriteRepository favoriteRepository;

    public AddFavoriteAdventureHandler(AdventureRepository adventureQueryRepository,
            FavoriteRepository favoriteRepository) {
        this.adventureQueryRepository = adventureQueryRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @Override
    public Void execute(AddFavoriteAdventure command) {

        Adventure adventure = adventureQueryRepository.findById(command.getAssetId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_BE_FOUND));

        if (!adventure.canUserRead(command.getPlayerId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION);
        }

        favoriteRepository.save(FavoriteEntity.builder()
                .assetType(ASSET_TYPE)
                .assetId(command.getAssetId())
                .playerId(command.getPlayerId())
                .build());

        return null;
    }
}
