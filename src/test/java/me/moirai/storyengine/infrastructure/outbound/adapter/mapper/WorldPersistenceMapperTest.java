package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldsResult;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;

@ExtendWith(MockitoExtension.class)
public class WorldPersistenceMapperTest {

    @InjectMocks
    private WorldPersistenceMapper mapper;

    @Test
    public void mapWorldDomain_whenGetOperation_thenMapToGetResult() {

        // Given
        World world = WorldFixture.privateWorld().build();

        // When
        WorldDetails result = mapper.mapToResult(world);

        // Then
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

    @Test
    public void mapWorldDomain_whenSearchWorld_thenMapToServer() {

        // Given
        List<World> worlds = IntStream.range(0, 20)
                .mapToObj(op -> WorldFixture.privateWorld()
                        .build())
                .toList();

        Pageable pageable = Pageable.ofSize(10);
        Page<World> page = new PageImpl<>(worlds, pageable, 20);

        // When
        SearchWorldsResult result = mapper.mapToResult(page);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.items()).isEqualTo(20);
    }
}
