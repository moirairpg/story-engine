package me.moirai.storyengine.infrastructure.security.authorization;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class AssetAuthorizerFactory {

    private final Map<String, ? extends BaseAssetAuthorizer> authorizers;

    public AssetAuthorizerFactory(List<? extends BaseAssetAuthorizer> authorizers) {

        this.authorizers = authorizers.stream()
                .collect(toMap(e -> e.getAssetType(), e -> e));
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseAssetAuthorizer> T getAuthorizerByAssetType(String assetType) {

        if (!authorizers.containsKey(assetType)) {
            throw new IllegalStateException("No authorizer defined for asset: " + assetType);
        }

        return (T) authorizers.get(assetType);
    }
}