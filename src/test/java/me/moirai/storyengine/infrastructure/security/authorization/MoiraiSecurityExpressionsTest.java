package me.moirai.storyengine.infrastructure.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.infrastructure.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.infrastructure.security.authentication.SecuritySessionContext;
import me.moirai.storyengine.infrastructure.security.authorization.authorizer.PersonaAuthorizer;

@ExtendWith(MockitoExtension.class)
public class MoiraiSecurityExpressionsTest {

    @Mock
    private MoiraiPrincipal principal;

    @Mock
    private AssetAuthorizerFactory authorizerFactory;

    @InjectMocks
    private MoiraiSecurityExpressions securityExpressions;

    @Test
    public void canRead() {

        // Given
        String userId = "12345";
        String assetType = "Persona";

        boolean canRead = true;
        PersonaAuthorizer authorizer = mock(PersonaAuthorizer.class);

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getDiscordId()).thenReturn(userId);
        when(authorizerFactory.getAuthorizerByAssetType(anyString())).thenReturn(authorizer);
        when(authorizer.canRead(anyString(), anyString())).thenReturn(canRead);

        // When
        boolean result = securityExpressions.canRead(userId, assetType);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void canModify() {

        // Given
        String userId = "12345";
        String assetType = "Persona";

        boolean canModify = true;
        PersonaAuthorizer authorizer = mock(PersonaAuthorizer.class);

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getDiscordId()).thenReturn(userId);
        when(authorizerFactory.getAuthorizerByAssetType(anyString())).thenReturn(authorizer);
        when(authorizer.canModify(anyString(), anyString())).thenReturn(canModify);

        // When
        boolean result = securityExpressions.canModify(userId, assetType);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void isAdmin() {

        // Given
        String role = "ADMIN";

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(role);

        // When
        boolean result = securityExpressions.isAdmin();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void isAuthenticatedUser() {

        // Given
        String userId = "1234";
        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getDiscordId()).thenReturn(userId);

        // When
        boolean result = securityExpressions.isAuthenticatedUser(userId);

        // Then
        assertThat(result).isTrue();
    }
}
