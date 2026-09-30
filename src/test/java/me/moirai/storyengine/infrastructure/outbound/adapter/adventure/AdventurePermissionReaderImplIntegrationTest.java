package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractDatabaseIntegrationTest;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.AssetMember;
import me.moirai.storyengine.core.port.outbound.adventure.AdventurePermissionReader;

public class AdventurePermissionReaderImplIntegrationTest extends AbstractDatabaseIntegrationTest {

    @Autowired
    private AdventurePermissionReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void getAllByAdventurePublicId_whenAdventureNotFound_thenReturnEmptyList() {

        // Given
        var publicId = UUID.randomUUID();

        // When
        var result = reader.getAllByAdventurePublicId(publicId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void getAllByAdventurePublicId_whenAdventureHasMembers_thenReturnUsernamesAndLevels() {

        // Given
        var owner = insert(UserFixture.player().username("owner").discordId("adventure-owner").build(), User.class);
        var member = insert(UserFixture.player().username("member").discordId("adventure-member").build(), User.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);

        var adventure = AdventureFixture.privateAdventure()
                .worldId(world.getPublicId())
                .permissions(new Permission(owner.getId(), PermissionLevel.OWNER))
                .permissions(new Permission(member.getId(), PermissionLevel.WRITE))
                .build();

        insert(adventure, Adventure.class);

        // When
        var result = reader.getAllByAdventurePublicId(adventure.getPublicId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AssetMember::username).containsExactly("owner", "member");
        assertThat(result.getFirst().level()).isEqualTo(PermissionLevel.OWNER);
        assertThat(result.getFirst().userId()).isEqualTo(owner.getPublicId());
    }

    @Test
    public void getAllByAdventurePublicId_whenAdventureHasSeveralNonOwners_thenOrderThemByDisplayName() {

        // Given
        var owner = insert(UserFixture.player().username("owner").displayName("Owner")
                .discordId("adv-display-owner").build(), User.class);
        var zoe = insert(UserFixture.player().username("zoe").displayName("Abigail")
                .discordId("adv-display-zoe").build(), User.class);
        var adam = insert(UserFixture.player().username("adam").displayName("Zachary")
                .discordId("adv-display-adam").build(), User.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);

        var adventure = AdventureFixture.privateAdventure()
                .worldId(world.getPublicId())
                .permissions(new Permission(owner.getId(), PermissionLevel.OWNER))
                .permissions(new Permission(zoe.getId(), PermissionLevel.READ))
                .permissions(new Permission(adam.getId(), PermissionLevel.WRITE))
                .build();

        insert(adventure, Adventure.class);

        // When
        var result = reader.getAllByAdventurePublicId(adventure.getPublicId());

        // Then
        assertThat(result).extracting(AssetMember::username).containsExactly("owner", "zoe", "adam");
        assertThat(result).extracting(AssetMember::displayName).containsExactly("Owner", "Abigail", "Zachary");
    }

    @Test
    public void getAllByAdventurePublicId_whenNonOwnersShareADisplayName_thenOrderThemByUsername() {

        // Given
        var owner = insert(UserFixture.player().username("owner").discordId("adv-order-owner").build(), User.class);
        var zoe = insert(UserFixture.player().username("zoe").discordId("adv-order-zoe").build(), User.class);
        var adam = insert(UserFixture.player().username("adam").discordId("adv-order-adam").build(), User.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);

        var adventure = AdventureFixture.privateAdventure()
                .worldId(world.getPublicId())
                .permissions(new Permission(owner.getId(), PermissionLevel.OWNER))
                .permissions(new Permission(zoe.getId(), PermissionLevel.READ))
                .permissions(new Permission(adam.getId(), PermissionLevel.WRITE))
                .build();

        insert(adventure, Adventure.class);

        // When
        var result = reader.getAllByAdventurePublicId(adventure.getPublicId());

        // Then
        assertThat(result).extracting(AssetMember::username).containsExactly("owner", "adam", "zoe");
    }
}
