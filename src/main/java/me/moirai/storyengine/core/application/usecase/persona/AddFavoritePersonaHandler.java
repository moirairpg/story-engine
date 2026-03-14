package me.moirai.storyengine.core.application.usecase.persona;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.persona.AddFavoritePersona;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.infrastructure.outbound.adapter.favorite.FavoriteEntity;
import me.moirai.storyengine.infrastructure.outbound.adapter.favorite.FavoriteRepository;

@UseCaseHandler
public class AddFavoritePersonaHandler extends AbstractUseCaseHandler<AddFavoritePersona, Void> {

    private static final String ASSET_TYPE = "persona";
    private static final String USER_NO_PERMISSION_IN_PERSONA = "User does not have permission to view the persona to be linked to this adventure";

    private final PersonaRepository personaRepository;
    private final FavoriteRepository favoriteRepository;

    public AddFavoritePersonaHandler(
            PersonaRepository personaRepository,
            FavoriteRepository favoriteRepository) {

        this.personaRepository = personaRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @Override
    public Void execute(AddFavoritePersona command) {

        Persona persona = personaRepository.findById(command.getAssetId())
                .orElseThrow(() -> new AssetNotFoundException("The persona to be favorited could not be found"));

        if (!persona.canUserRead(command.getPlayerId())) {
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
