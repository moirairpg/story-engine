package me.moirai.storyengine.infrastructure.security.authorization;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.infrastructure.security.authorization.authorizer.PersonaAuthorizer;

@ExtendWith(MockitoExtension.class)
public class AssetAuthorizerFactoryTest {

    @Mock
    private UseCaseRunner useCaseRunner;

    @Test
    public void getAuthorizer_whenAuthorizerDefined_thenReturnAuthorizer() {

        // Given
        String assetType = "Persona";

        PersonaAuthorizer personaAuthorizer = new PersonaAuthorizer(useCaseRunner);
        List<PersonaAuthorizer> authorizers = new ArrayList<>();

        authorizers.add(personaAuthorizer);

        AssetAuthorizerFactory factory = new AssetAuthorizerFactory(authorizers);

        // When
        PersonaAuthorizer result = factory.getAuthorizerByAssetType(assetType);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void getAuthorizer_whenAuthorizerNotFound_thenThrowException() {

        // Given
        String assetType = "Persona";

        List<PersonaAuthorizer> authorizers = emptyList();

        AssetAuthorizerFactory factory = new AssetAuthorizerFactory(authorizers);

        // Then
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> factory.getAuthorizerByAssetType(assetType));
    }
}
