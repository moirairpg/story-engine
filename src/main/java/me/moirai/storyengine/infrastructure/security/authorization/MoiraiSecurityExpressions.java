package me.moirai.storyengine.infrastructure.security.authorization;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.infrastructure.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.infrastructure.security.authentication.SecuritySessionContext;

@Component
public class MoiraiSecurityExpressions extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

    private static final String ADMIN = "ADMIN";

    private Object filterObject;
    private Object returnObject;

    private final AssetAuthorizerFactory authorizerFactory;

    public MoiraiSecurityExpressions(AssetAuthorizerFactory authorizerFactory) {

        super(SecuritySessionContext.getAuthenticationContext());
        this.authorizerFactory = authorizerFactory;
    }

    public boolean isAdmin() {
        MoiraiPrincipal principal = SecuritySessionContext.getAuthenticatedUser();
        return principal.getRole().equals(ADMIN);
    }

    public boolean isAuthenticatedUser(String userId) {
        MoiraiPrincipal principal = SecuritySessionContext.getAuthenticatedUser();
        return principal.getDiscordId().equals(userId);
    }

    public boolean canModify(String assetId, String assetType) {

        MoiraiPrincipal principal = SecuritySessionContext.getAuthenticatedUser();
        return authorizerFactory.getAuthorizerByAssetType(assetType)
                .canModify(assetId, principal.getDiscordId());
    }

    public boolean canRead(String assetId, String assetType) {

        MoiraiPrincipal principal = SecuritySessionContext.getAuthenticatedUser();
        return authorizerFactory.getAuthorizerByAssetType(assetType)
                .canRead(assetId, principal.getDiscordId());
    }

    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    @Override
    public MoiraiSecurityExpressions getThis() {
        return this;
    }
}
