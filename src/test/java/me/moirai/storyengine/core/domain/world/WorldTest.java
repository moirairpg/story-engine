package me.moirai.storyengine.core.domain.world;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.core.domain.Permissions;
import me.moirai.storyengine.core.domain.PermissionsFixture;

public class WorldTest {

    @Test
    public void makeWorldPublic() {

        // Given
        World world = WorldFixture.privateWorld().build();

        // When
        world.makePublic();

        // Then
        assertThat(world.isPublic()).isTrue();
    }

    @Test
    public void makeWorldPrivate() {

        // Given
        World world = WorldFixture.publicWorld().build();

        // When
        world.makePrivate();

        // Then
        assertThat(world.isPublic()).isFalse();
    }

    @Test
    public void updateWorldName() {

        // Given
        World world = WorldFixture.publicWorld().build();

        // When
        world.updateName("New Name");

        // Then
        assertThat(world.getName()).isEqualTo("New Name");
    }

    @Test
    public void updateWorldDescription() {

        // Given
        World world = WorldFixture.publicWorld().build();

        // When
        world.updateDescription("New Description");

        // Then
        assertThat(world.getDescription()).isEqualTo("New Description");
    }

    @Test
    public void updateWorldInitialPrompt() {

        // Given
        World world = WorldFixture.publicWorld().build();

        // When
        world.updateAdventureStart("New Prompt");

        // Then
        assertThat(world.getAdventureStart()).isEqualTo("New Prompt");
    }

    @Test
    public void errorWhenCreatingWorldWithNullName() {

        // Given
        World.Builder worldBuilder = WorldFixture.publicWorld().name(null);

        // Then
        assertThrows(BusinessRuleViolationException.class, worldBuilder::build);
    }

    @Test
    public void errorWhenCreatingWorldWithEmptyName() {

        // Given
        World.Builder worldBuilder = WorldFixture.publicWorld().name(EMPTY);

        // Then
        assertThrows(BusinessRuleViolationException.class, worldBuilder::build);
    }

    @Test
    public void errorWhenCreatingWorldWithNullPermissions() {

        // Given
        World.Builder worldBuilder = WorldFixture.publicWorld().permissions(null);

        // Then
        assertThrows(BusinessRuleViolationException.class, worldBuilder::build);
    }

    @Test
    public void errorWhenCreatingWorldWithNullVisibility() {

        // Given
        World.Builder worldBuilder = WorldFixture.publicWorld().visibility(null);

        // Then
        assertThrows(BusinessRuleViolationException.class, worldBuilder::build);
    }

    @Test
    public void addWriterToList() {

        // Given
        String userId = "1234567890";
        World.Builder worldBuilder = WorldFixture.publicWorld();
        Permissions permissions = PermissionsFixture.samplePermissions()
                .usersAllowedToWrite(new HashSet<>()).build();

        worldBuilder.permissions(permissions);

        World world = worldBuilder.build();

        // When
        world.addWriterUser(userId);

        // Then
        assertThat(world.getUsersAllowedToWrite()).contains(userId);
    }

    @Test
    public void addReaderToList() {

        // Given
        String userId = "1234567890";
        World.Builder worldBuilder = WorldFixture.publicWorld();
        Permissions permissions = PermissionsFixture.samplePermissions()
                .usersAllowedToRead(new HashSet<>()).build();

        worldBuilder.permissions(permissions);

        World world = worldBuilder.build();

        // When
        world.addReaderUser(userId);

        // Then
        assertThat(world.getUsersAllowedToRead()).contains(userId);
    }

    @Test
    public void removeReaderFromList() {

        // Given
        String userId = "1234567890";
        World.Builder worldBuilder = WorldFixture.publicWorld();

        Set<String> usersAllowedToRead = new HashSet<>();
        usersAllowedToRead.add(userId);

        Permissions permissions = PermissionsFixture.samplePermissions()
                .usersAllowedToRead(usersAllowedToRead).build();

        worldBuilder.permissions(permissions);

        World world = worldBuilder.build();

        // When
        world.removeReaderUser(userId);

        // Then
        assertThat(world.getUsersAllowedToRead()).doesNotContain(userId);
    }

    @Test
    public void removeWriterFromList() {

        // Given
        String userId = "1234567890";
        World.Builder worldBuilder = WorldFixture.publicWorld();

        Set<String> usersAllowedToWrite = new HashSet<>();
        usersAllowedToWrite.add(userId);

        Permissions permissions = PermissionsFixture.samplePermissions()
                .usersAllowedToWrite(usersAllowedToWrite).build();

        worldBuilder.permissions(permissions);

        World world = worldBuilder.build();

        // When
        world.removeWriterUser(userId);

        // Then
        assertThat(world.getUsersAllowedToWrite()).doesNotContain(userId);
    }

    @Test
    public void userIsOwner() {

        // Given
        String testedUserId = "1234567890";
        Permissions permissions = PermissionsFixture.samplePermissions()
                .ownerId(testedUserId)
                .build();

        World world = WorldFixture.privateWorld().permissions(permissions).build();

        // When
        boolean isUserOwner = world.isOwner(testedUserId);

        // Then
        assertThat(isUserOwner).isTrue();
    }

    @Test
    public void userCanWriteisWriter() {

        // Given
        String testedUserId = "1234567890";
        Permissions permissions = PermissionsFixture.samplePermissions()
                .usersAllowedToWrite(Collections.singleton(testedUserId))
                .build();

        World world = WorldFixture.privateWorld().permissions(permissions).build();

        // When
        boolean isUserWriter = world.canUserWrite(testedUserId);

        // Then
        assertThat(isUserWriter).isTrue();
    }

    @Test
    public void userCanWriteIsOwner() {

        // Given
        String testedUserId = "1234567890";
        Permissions permissions = PermissionsFixture.samplePermissions()
                .ownerId(testedUserId)
                .build();

        World world = WorldFixture.privateWorld().permissions(permissions).build();

        // When
        boolean isUserWriter = world.canUserWrite(testedUserId);

        // Then
        assertThat(isUserWriter).isTrue();
    }

    @Test
    public void userCanReadisWriter() {

        // Given
        String testedUserId = "1234567890";
        Permissions permissions = PermissionsFixture.samplePermissions()
                .usersAllowedToWrite(Collections.singleton(testedUserId))
                .build();

        World world = WorldFixture.privateWorld().permissions(permissions).build();

        // When
        boolean isUserReader = world.canUserRead(testedUserId);

        // Then
        assertThat(isUserReader).isTrue();
    }

    @Test
    public void userCanReadisReader() {

        // Given
        String testedUserId = "1234567890";
        Permissions permissions = PermissionsFixture.samplePermissions()
                .usersAllowedToRead(Collections.singleton(testedUserId))
                .build();

        World world = WorldFixture.privateWorld().permissions(permissions).build();

        // When
        boolean isUserReader = world.canUserRead(testedUserId);

        // Then
        assertThat(isUserReader).isTrue();
    }

    @Test
    public void userCanReadIsOwner() {

        // Given
        String testedUserId = "1234567890";
        Permissions permissions = PermissionsFixture.samplePermissions()
                .ownerId(testedUserId)
                .build();

        World world = WorldFixture.privateWorld().permissions(permissions).build();

        // When
        boolean isUserReader = world.canUserRead(testedUserId);

        // Then
        assertThat(isUserReader).isTrue();
    }

    @Test
    public void userCannotWrite() {

        // Given
        String testedUserId = "1234567890";
        Permissions permissions = PermissionsFixture.samplePermissions().build();
        World world = WorldFixture.privateWorld().permissions(permissions).build();

        // When
        boolean isUserWriter = world.canUserWrite(testedUserId);

        // Then
        assertThat(isUserWriter).isFalse();
    }

    @Test
    public void userCannotRead() {

        // Given
        String testedUserId = "1234567890";
        Permissions permissions = PermissionsFixture.samplePermissions().build();
        World world = WorldFixture.privateWorld().permissions(permissions).build();

        // When
        boolean isUserReader = world.canUserRead(testedUserId);

        // Then
        assertThat(isUserReader).isFalse();
    }
}
