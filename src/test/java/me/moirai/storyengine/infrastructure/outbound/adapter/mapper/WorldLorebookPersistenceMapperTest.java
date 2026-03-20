package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.domain.world.WorldLorebookEntryFixture;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;

@ExtendWith(MockitoExtension.class)
public class WorldLorebookPersistenceMapperTest {

    @InjectMocks
    private WorldLorebookPersistenceMapper mapper;

    @Test
    public void mapWorldLorebookEntryDomain_whenGetOperation_thenMapToGetResult() {

        var worldLorebookEntry = WorldLorebookEntryFixture.sampleLorebookEntry().build();

        WorldLorebookEntryDetails result = mapper.mapToResult(worldLorebookEntry);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(worldLorebookEntry.getName());
        assertThat(result.regex()).isEqualTo(worldLorebookEntry.getRegex());
        assertThat(result.description()).isEqualTo(worldLorebookEntry.getDescription());
    }
}
