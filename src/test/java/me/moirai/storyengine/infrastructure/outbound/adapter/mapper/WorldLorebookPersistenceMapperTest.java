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

import me.moirai.storyengine.core.port.inbound.world.GetWorldLorebookEntryResult;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntriesResult;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntryFixture;

@ExtendWith(MockitoExtension.class)
public class WorldLorebookPersistenceMapperTest {

    @InjectMocks
    private WorldLorebookPersistenceMapper mapper;

    @Test
    public void mapWorldLorebookEntryDomain_whenGetOperation_thenMapToGetResult() {

        // Given
        WorldLorebookEntry worldLorebookEntry = WorldLorebookEntryFixture.sampleLorebookEntry().build();

        // When
        GetWorldLorebookEntryResult result = mapper.mapToResult(worldLorebookEntry);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(worldLorebookEntry.getName());
        assertThat(result.getRegex()).isEqualTo(worldLorebookEntry.getRegex());
        assertThat(result.getDescription()).isEqualTo(worldLorebookEntry.getDescription());
    }

    @Test
    public void mapWorldLorebookEntryDomain_whenSearchWorldLorebookEntry_thenMapToServer() {

        // Given
        List<WorldLorebookEntry> worldLorebookEntries = IntStream.range(0, 20)
                .mapToObj(op -> WorldLorebookEntryFixture.sampleLorebookEntry()
                        .id(String.valueOf(op + 1))
                        .build())
                .toList();

        Pageable pageable = Pageable.ofSize(10);
        Page<WorldLorebookEntry> page = new PageImpl<>(worldLorebookEntries, pageable, 20);

        // When
        SearchWorldLorebookEntriesResult result = mapper.mapToResult(page);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getItems()).isEqualTo(20);
    }
}
