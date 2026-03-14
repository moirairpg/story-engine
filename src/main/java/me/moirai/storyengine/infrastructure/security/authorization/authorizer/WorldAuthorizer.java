package me.moirai.storyengine.infrastructure.security.authorization.authorizer;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.core.port.inbound.world.GetWorldById;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.infrastructure.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.infrastructure.security.authentication.SecuritySessionContext;
import me.moirai.storyengine.infrastructure.security.authorization.BaseAssetAuthorizer;

@Component
public class WorldAuthorizer implements BaseAssetAuthorizer {

    private static final String ADMIN = "ADMIN";
    private static final String PUBLIC = "PUBLIC";

    private final UseCaseRunner useCaseRunner;

    public WorldAuthorizer(UseCaseRunner useCaseRunner) {
        this.useCaseRunner = useCaseRunner;
    }

    @Override
    public String getAssetType() {
        return "World";
    }

    @Override
    public boolean isOwner(String assetId, String userId) {

        MoiraiPrincipal principal = SecuritySessionContext.getAuthenticatedUser();
        GetWorldById request = GetWorldById.build(assetId, principal.getDiscordId());
        WorldDetails worldDetails = useCaseRunner.run(request);

        boolean isAdmin = principal.getRole().equals(ADMIN);
        boolean isOwner = worldDetails.getOwnerId().equals(userId);

        return isAdmin || isOwner;
    }

    @Override
    public boolean canModify(String assetId, String userId) {

        MoiraiPrincipal principal = SecuritySessionContext.getAuthenticatedUser();
        GetWorldById request = GetWorldById.build(assetId, principal.getDiscordId());
        WorldDetails worldDetails = useCaseRunner.run(request);

        boolean isAdmin = principal.getRole().equals(ADMIN);
        boolean isOwner = worldDetails.getOwnerId().equals(userId);
        boolean isWriter = worldDetails.getUsersAllowedToWrite().contains(userId);

        return isAdmin || isOwner || isWriter;
    }

    @Override
    public boolean canRead(String assetId, String userId) {

        MoiraiPrincipal principal = SecuritySessionContext.getAuthenticatedUser();
        GetWorldById request = GetWorldById.build(assetId, principal.getDiscordId());
        WorldDetails worldDetails = useCaseRunner.run(request);

        boolean isAdmin = principal.getRole().equals(ADMIN);
        boolean isOwner = worldDetails.getOwnerId().equals(userId);
        boolean isWriter = worldDetails.getUsersAllowedToWrite().contains(userId);
        boolean isReader = worldDetails.getUsersAllowedToRead().contains(userId);
        boolean isPublic = worldDetails.getVisibility().equalsIgnoreCase(PUBLIC);

        return isAdmin || isOwner || isWriter || isReader || isPublic;
    }
}
