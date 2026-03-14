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
import me.moirai.storyengine.core.application.usecase.world.result.GetWorldResult;
import me.moirai.storyengine.core.application.usecase.world.result.GetWorldResultFixture;
import me.moirai.storyengine.infrastructure.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.infrastructure.security.authentication.SecuritySessionContext;

@ExtendWith(MockitoExtension.class)
public class WorldAuthorizerTest {

    private static final String ADMIN = "ADMIN";
    private static final String USER = "USER";

    @Mock
    private MoiraiPrincipal principal;

    @Mock
    private UseCaseRunner useCaseRunner;

    @InjectMocks
    private WorldAuthorizer authorizer;

    @Test
    public void authorizeWorld_whenGetAssetType_thenReturnCorrectType() {

        // Given
        String expectedAssetType = "World";

        // Then
        assertThat(authorizer.getAssetType()).isEqualTo(expectedAssetType);
    }

    @Test
    public void authorizeWorld_whenCheckOwnership_andUserIsOwner_thenAuthorize() {

        // Given
        String worldId = "12345";
        String userId = "12345";
        GetWorldResult worldDetails = GetWorldResultFixture.privateWorld()
                .ownerId(userId)
                .build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(worldDetails);

        // When
        boolean result = authorizer.isOwner(worldId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeWorld_whenCheckOwnership_andUserIsAdmin_thenAuthorize() {

        // Given
        String worldId = "12345";
        String userId = "12345";
        GetWorldResult worldDetails = GetWorldResultFixture.privateWorld().build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(ADMIN);
        when(useCaseRunner.run(any())).thenReturn(worldDetails);

        // When
        boolean result = authorizer.isOwner(worldId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeWorld_whenCheckOwnership_andUserIsNotOwner_thenDontAuthorize() {

        // Given
        String worldId = "12345";
        String userId = "12345";
        GetWorldResult worldDetails = GetWorldResultFixture.privateWorld().build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(worldDetails);

        // When
        boolean result = authorizer.isOwner(worldId, userId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void authorizeWorld_whenCheckModifyingRights_andUserIsOwner_thenAuthorize() {

        // Given
        String worldId = "12345";
        String userId = "12345";
        GetWorldResult worldDetails = GetWorldResultFixture.privateWorld()
                .ownerId(userId)
                .build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(worldDetails);

        // When
        boolean result = authorizer.canModify(worldId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeWorld_whenCheckModifyingRights_andUserIsAdmin_thenAuthorize() {

        // Given
        String worldId = "12345";
        String userId = "12345";
        GetWorldResult worldDetails = GetWorldResultFixture.privateWorld().build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(ADMIN);
        when(useCaseRunner.run(any())).thenReturn(worldDetails);

        // When
        boolean result = authorizer.canModify(worldId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeWorld_whenCheckModifyingRights_andUserIsWriter_thenAuthorize() {

        // Given
        String worldId = "12345";
        String userId = "12345";
        GetWorldResult worldDetails = GetWorldResultFixture.privateWorld()
                .usersAllowedToWrite(set(userId))
                .build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(worldDetails);

        // When
        boolean result = authorizer.canModify(worldId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeWorld_whenCheckModifyingRights_andUserIsReader_thenDontAuthorize() {

        // Given
        String worldId = "12345";
        String userId = "12345";
        GetWorldResult worldDetails = GetWorldResultFixture.privateWorld()
                .usersAllowedToRead(set(userId))
                .build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(worldDetails);

        // When
        boolean result = authorizer.canModify(worldId, userId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void authorizeWorld_whenCheckReadingRights_andUserIsOwner_thenAuthorize() {

        // Given
        String worldId = "12345";
        String userId = "12345";
        GetWorldResult worldDetails = GetWorldResultFixture.privateWorld()
                .ownerId(userId)
                .build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(worldDetails);

        // When
        boolean result = authorizer.canRead(worldId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeWorld_whenCheckReadingRights_andUserIsAdmin_thenAuthorize() {

        // Given
        String worldId = "12345";
        String userId = "12345";
        GetWorldResult worldDetails = GetWorldResultFixture.privateWorld().build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(ADMIN);
        when(useCaseRunner.run(any())).thenReturn(worldDetails);

        // When
        boolean result = authorizer.canRead(worldId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeWorld_whenCheckReadingRights_andUserIsWriter_thenAuthorize() {

        // Given
        String worldId = "12345";
        String userId = "12345";
        GetWorldResult worldDetails = GetWorldResultFixture.privateWorld()
                .usersAllowedToWrite(set(userId))
                .build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(worldDetails);

        // When
        boolean result = authorizer.canRead(worldId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeWorld_whenCheckReadingRights_andUserIsReader_thenAuthorize() {

        // Given
        String worldId = "12345";
        String userId = "12345";
        GetWorldResult worldDetails = GetWorldResultFixture.privateWorld()
                .usersAllowedToRead(set(userId))
                .build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(worldDetails);

        // When
        boolean result = authorizer.canRead(worldId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeWorld_whenCheckReadingRights_andWorldIsPublic_thenAuthorize() {

        // Given
        String worldId = "12345";
        String userId = "12345";
        GetWorldResult worldDetails = GetWorldResultFixture.publicWorld().build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(worldDetails);

        // When
        boolean result = authorizer.canRead(worldId, userId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void authorizeWorld_whenCheckReadingRights_andWorldIsPrivate_andUserNotPermission_thenAuthorize() {

        // Given
        String worldId = "12345";
        String userId = "12345";
        GetWorldResult worldDetails = GetWorldResultFixture.privateWorld().build();

        SecuritySessionContext.setCurrentUser(principal);

        when(principal.getRole()).thenReturn(USER);
        when(useCaseRunner.run(any())).thenReturn(worldDetails);

        // When
        boolean result = authorizer.canRead(worldId, userId);

        // Then
        assertThat(result).isFalse();
    }
}
