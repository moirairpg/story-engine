package me.moirai.storyengine.infrastructure.outbound.adapter.world;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractDatabaseIntegrationTest;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.AssetMember;
import me.moirai.storyengine.core.port.outbound.world.WorldPermissionReader;

public class WorldPermissionReaderImplIntegrationTest extends AbstractDatabaseIntegrationTest {

    @Autowired
    private WorldPermissionReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void getAllByWorldPublicId_whenWorldNotFound_thenReturnEmptyList() {

        // Given
        var publicId = UUID.randomUUID();

        // When
        var result = reader.getAllByWorldPublicId(publicId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void getAllByWorldPublicId_whenWorldHasMembers_thenReturnUsernamesAndLevels() {

        // Given
        var owner = insert(UserFixture.player().username("owner").discordId("world-owner").build(), User.class);
        var member = insert(UserFixture.player().username("member").discordId("world-member").build(), User.class);

        var world = WorldFixture.privateWorld()
                .permissions(new Permission(owner.getId(), PermissionLevel.OWNER))
                .permissions(new Permission(member.getId(), PermissionLevel.WRITE))
                .build();

        insert(world, World.class);

        // When
        var result = reader.getAllByWorldPublicId(world.getPublicId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AssetMember::username).containsExactly("owner", "member");
        assertThat(result.getFirst().level()).isEqualTo(PermissionLevel.OWNER);
        assertThat(result.getFirst().userId()).isEqualTo(owner.getPublicId());
    }

    @Test
    public void getAllByWorldPublicId_whenWorldHasSeveralNonOwners_thenOrderThemByDisplayName() {

        // Given
        var owner = insert(UserFixture.player().username("owner").displayName("Owner")
                .discordId("world-display-owner").build(), User.class);
        var zoe = insert(UserFixture.player().username("zoe").displayName("Abigail")
                .discordId("world-display-zoe").build(), User.class);
        var adam = insert(UserFixture.player().username("adam").displayName("Zachary")
                .discordId("world-display-adam").build(), User.class);

        var world = WorldFixture.privateWorld()
                .permissions(new Permission(owner.getId(), PermissionLevel.OWNER))
                .permissions(new Permission(zoe.getId(), PermissionLevel.READ))
                .permissions(new Permission(adam.getId(), PermissionLevel.WRITE))
                .build();

        insert(world, World.class);

        // When
        var result = reader.getAllByWorldPublicId(world.getPublicId());

        // Then
        assertThat(result).extracting(AssetMember::username).containsExactly("owner", "zoe", "adam");
        assertThat(result).extracting(AssetMember::displayName).containsExactly("Owner", "Abigail", "Zachary");
    }

    @Test
    public void getAllByWorldPublicId_whenNonOwnersShareADisplayName_thenOrderThemByUsername() {

        // Given
        var owner = insert(UserFixture.player().username("owner").discordId("world-order-owner").build(), User.class);
        var zoe = insert(UserFixture.player().username("zoe").discordId("world-order-zoe").build(), User.class);
        var adam = insert(UserFixture.player().username("adam").discordId("world-order-adam").build(), User.class);

        var world = WorldFixture.privateWorld()
                .permissions(new Permission(owner.getId(), PermissionLevel.OWNER))
                .permissions(new Permission(zoe.getId(), PermissionLevel.READ))
                .permissions(new Permission(adam.getId(), PermissionLevel.WRITE))
                .build();

        insert(world, World.class);

        // When
        var result = reader.getAllByWorldPublicId(world.getPublicId());

        // Then
        assertThat(result).extracting(AssetMember::username).containsExactly("owner", "adam", "zoe");
    }
}
