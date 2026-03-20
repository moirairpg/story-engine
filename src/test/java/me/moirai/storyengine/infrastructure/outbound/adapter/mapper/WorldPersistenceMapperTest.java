package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;

@ExtendWith(MockitoExtension.class)
public class WorldPersistenceMapperTest {

    @InjectMocks
    private WorldPersistenceMapper mapper;

    @Test
    public void mapWorldDomain_whenGetOperation_thenMapToGetResult() {

        var world = WorldFixture.privateWorld().build();

        WorldDetails result = mapper.mapToResult(world);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(world.getName());
        assertThat(result.ownerId()).isEqualTo(world.getOwnerId());
        assertThat(result.creationDate()).isEqualTo(world.getCreationDate());
        assertThat(result.lastUpdateDate()).isEqualTo(world.getLastUpdateDate());
        assertThat(result.usersAllowedToRead()).hasSameElementsAs(world.getUsersAllowedToRead());
        assertThat(result.usersAllowedToWrite()).hasSameElementsAs(world.getUsersAllowedToWrite());
        assertThat(result.description()).isEqualTo(world.getDescription());
        assertThat(result.adventureStart()).isEqualTo(world.getAdventureStart());
    }
}
