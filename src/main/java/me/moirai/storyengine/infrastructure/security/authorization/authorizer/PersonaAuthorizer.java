package me.moirai.storyengine.infrastructure.security.authorization.authorizer;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.core.port.inbound.persona.GetPersonaById;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.infrastructure.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.infrastructure.security.authentication.SecuritySessionContext;
import me.moirai.storyengine.infrastructure.security.authorization.BaseAssetAuthorizer;

@Component
public class PersonaAuthorizer implements BaseAssetAuthorizer {

    private static final String ADMIN = "ADMIN";
    private static final String PUBLIC = "PUBLIC";

    private final UseCaseRunner useCaseRunner;

    public PersonaAuthorizer(UseCaseRunner useCaseRunner) {
        this.useCaseRunner = useCaseRunner;
    }

    @Override
    public String getAssetType() {
        return "Persona";
    }

    @Override
    public boolean isOwner(String assetId, String userId) {

        MoiraiPrincipal principal = SecuritySessionContext.getAuthenticatedUser();
        GetPersonaById request = GetPersonaById.build(assetId, principal.getDiscordId());
        PersonaDetails personaDetails = useCaseRunner.run(request);

        boolean isAdmin = principal.getRole().equals(ADMIN);
        boolean isOwner = personaDetails.getOwnerId().equals(userId);

        return isAdmin || isOwner;
    }

    @Override
    public boolean canModify(String assetId, String userId) {

        MoiraiPrincipal principal = SecuritySessionContext.getAuthenticatedUser();
        GetPersonaById request = GetPersonaById.build(assetId, principal.getDiscordId());
        PersonaDetails personaDetails = useCaseRunner.run(request);

        boolean isAdmin = principal.getRole().equals(ADMIN);
        boolean isOwner = personaDetails.getOwnerId().equals(userId);
        boolean isWriter = personaDetails.getUsersAllowedToWrite().contains(userId);

        return isAdmin || isOwner || isWriter;
    }

    @Override
    public boolean canRead(String assetId, String userId) {

        MoiraiPrincipal principal = SecuritySessionContext.getAuthenticatedUser();
        GetPersonaById request = GetPersonaById.build(assetId, principal.getDiscordId());
        PersonaDetails personaDetails = useCaseRunner.run(request);

        boolean isAdmin = principal.getRole().equals(ADMIN);
        boolean isOwner = personaDetails.getOwnerId().equals(userId);
        boolean isWriter = personaDetails.getUsersAllowedToWrite().contains(userId);
        boolean isReader = personaDetails.getUsersAllowedToRead().contains(userId);
        boolean isPublic = personaDetails.getVisibility().equalsIgnoreCase(PUBLIC);

        return isAdmin || isOwner || isWriter || isReader || isPublic;
    }
}
