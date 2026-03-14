package me.moirai.storyengine.infrastructure.security.authorization.authorizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.set;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.application.usecase.adventure.result.GetAdventureResultFixture;
import me.moirai.storyengine.infrastructure.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.infrastructure.security.authentication.SecuritySessionContext;

@ExtendWith(MockitoExtension.class)
public class AdventureAuthorizerTest {

    private static final String ADMIN = "ADMIN";
    private static final String USER = "USER";

    @Mock
    private MoiraiPrincipal principal;

    @Mock
    private UseCaseRunner useCaseRunner;

    @InjectMocks
    private AdventureAuthorizer authorizer;

    @Test
    public void authorizeAdventure_whenGetAssetType_thenReturnCorrectType() {

        // Given
        String expectedAssetType = "Adventure";

        // Then
        assertThat(authorizer.getAssetType()).isEqualTo(expectedAssetType);
    }

    @Test
    public void authorizeAdventure_whenCheckOwnership_andUserIsOwner_thenAuthorize() {

        // Given
        String adventureId = "12345";
        String userId = "12345";
        AdventureDetails adventureDetails = GetAdventureResultFixture.privateMultiplayerAdventure()
                .ownerId(userId)
                .build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(adventureDetails);

        // When
        boolean result = authorizer.isOwner(adventureId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeAdventure_whenCheckOwnership_andUserIsAdmin_thenAuthorize() {

        // Given
        String adventureId = "12345";
        String userId = "12345";
        AdventureDetails adventureDetails = GetAdventureResultFixture.privateMultiplayerAdventure().build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(ADMIN);
        when(useCaseRunner.run(any())).thenReturn(adventureDetails);

        // When
        boolean result = authorizer.isOwner(adventureId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeAdventure_whenCheckOwnership_andUserIsNotOwner_thenDontAuthorize() {

        // Given
        String adventureId = "12345";
        String userId = "12345";
        AdventureDetails adventureDetails = GetAdventureResultFixture.privateMultiplayerAdventure().build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(adventureDetails);

        // When
        boolean result = authorizer.isOwner(adventureId, userId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void authorizeAdventure_whenCheckModifyingRights_andUserIsOwner_thenAuthorize() {

        // Given
        String adventureId = "12345";
        String userId = "12345";
        AdventureDetails adventureDetails = GetAdventureResultFixture.privateMultiplayerAdventure()
                .ownerId(userId)
                .build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(adventureDetails);

        // When
        boolean result = authorizer.canModify(adventureId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeAdventure_whenCheckModifyingRights_andUserIsAdmin_thenAuthorize() {

        // Given
        String adventureId = "12345";
        String userId = "12345";
        AdventureDetails adventureDetails = GetAdventureResultFixture.privateMultiplayerAdventure().build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(ADMIN);
        when(useCaseRunner.run(any())).thenReturn(adventureDetails);

        // When
        boolean result = authorizer.canModify(adventureId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeAdventure_whenCheckModifyingRights_andUserIsWriter_thenAuthorize() {

        // Given
        String adventureId = "12345";
        String userId = "12345";
        AdventureDetails adventureDetails = GetAdventureResultFixture.privateMultiplayerAdventure()
                .usersAllowedToWrite(set(userId))
                .build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(adventureDetails);

        // When
        boolean result = authorizer.canModify(adventureId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeAdventure_whenCheckModifyingRights_andUserIsReader_thenDontAuthorize() {

        // Given
        String adventureId = "12345";
        String userId = "12345";
        AdventureDetails adventureDetails = GetAdventureResultFixture.privateMultiplayerAdventure()
                .usersAllowedToRead(set(userId))
                .build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(adventureDetails);

        // When
        boolean result = authorizer.canModify(adventureId, userId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void authorizeAdventure_whenCheckReadingRights_andUserIsOwner_thenAuthorize() {

        // Given
        String adventureId = "12345";
        String userId = "12345";
        AdventureDetails adventureDetails = GetAdventureResultFixture.privateMultiplayerAdventure()
                .ownerId(userId)
                .build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(adventureDetails);

        // When
        boolean result = authorizer.canRead(adventureId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeAdventure_whenCheckReadingRights_andUserIsAdmin_thenAuthorize() {

        // Given
        String adventureId = "12345";
        String userId = "12345";
        AdventureDetails adventureDetails = GetAdventureResultFixture.privateMultiplayerAdventure().build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(ADMIN);
        when(useCaseRunner.run(any())).thenReturn(adventureDetails);

        // When
        boolean result = authorizer.canRead(adventureId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeAdventure_whenCheckReadingRights_andUserIsWriter_thenAuthorize() {

        // Given
        String adventureId = "12345";
        String userId = "12345";
        AdventureDetails adventureDetails = GetAdventureResultFixture.privateMultiplayerAdventure()
                .usersAllowedToWrite(set(userId))
                .build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(adventureDetails);

        // When
        boolean result = authorizer.canRead(adventureId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeAdventure_whenCheckReadingRights_andUserIsReader_thenAuthorize() {

        // Given
        String adventureId = "12345";
        String userId = "12345";
        AdventureDetails adventureDetails = GetAdventureResultFixture.privateMultiplayerAdventure()
                .usersAllowedToRead(set(userId))
                .build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(adventureDetails);

        // When
        boolean result = authorizer.canRead(adventureId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeAdventure_whenCheckReadingRights_andAdventureIsPublic_thenAuthorize() {

        // Given
        String adventureId = "12345";
        String userId = "12345";
        AdventureDetails adventureDetails = GetAdventureResultFixture.publicMultiplayerAdventure().build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(adventureDetails);

        // When
        boolean result = authorizer.canRead(adventureId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeAdventure_whenCheckReadingRights_andAdventureIsPrivate_andUserNotPermission_thenAuthorize() {

        // Given
        String adventureId = "12345";
        String userId = "12345";
        AdventureDetails adventureDetails = GetAdventureResultFixture.privateMultiplayerAdventure().build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(adventureDetails);

        // When
        boolean result = authorizer.canRead(adventureId, userId);

        // Then
        assertThat(result).isFalse();
    }
}
