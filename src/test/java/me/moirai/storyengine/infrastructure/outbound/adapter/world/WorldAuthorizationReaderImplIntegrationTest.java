package me.moirai.storyengine.infrastructure.outbound.adapter.world;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.outbound.world.WorldAuthorizationReader;

public class WorldAuthorizationReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WorldAuthorizationReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void getAuthorizationData_whenWorldNotFound_thenReturnEmpty() {

        // Given
        var publicId = UUID.randomUUID();

        // When
        var result = reader.getAuthorizationData(publicId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void getAuthorizationData_whenWorldFound_thenReturnVisibilityAndEmptyPermissions() {

        // Given
        var world = insert(WorldFixture.publicWorld().build(), World.class);

        // When
        var result = reader.getAuthorizationData(world.getPublicId());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get().ownerId()).isNull();
        assertThat(result.get().writers()).isEmpty();
        assertThat(result.get().readers()).isEmpty();
        assertThat(result.get().visibility()).isEqualTo(Visibility.PUBLIC);
    }

    @Test
    public void getAuthorizationData_whenWorldHasWriter_thenReturnWriterPublicId() {

        // Given
        var user = insert(UserFixture.player().build(), User.class);
        var world = WorldFixture.publicWorld()
                .permissions(new Permission(user.getId(), PermissionLevel.WRITE))
                .build();

        insert(world, World.class);

        // When
        var result = reader.getAuthorizationData(world.getPublicId());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get().writers()).containsExactly(user.getPublicId());
    }
}
