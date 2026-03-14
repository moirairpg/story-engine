package me.moirai.storyengine.infrastructure.outbound.persistence.mapper;

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

import me.moirai.storyengine.core.application.usecase.adventure.result.GetAdventureResult;
import me.moirai.storyengine.core.application.usecase.adventure.result.SearchAdventuresResult;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;

@ExtendWith(MockitoExtension.class)
public class AdventurePersistenceMapperTest {

    @InjectMocks
    private AdventurePersistenceMapper mapper;

    @Test
    public void mapAdventureDomain_whenGetOperation_thenMapToGetResult() {

        // Given
        Adventure adventure = AdventureFixture.publicSingleplayerAdventure().build();

        // When
        GetAdventureResult result = mapper.mapToResult(adventure);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(adventure.getId());
        assertThat(result.getName()).isEqualTo(adventure.getName());
        assertThat(result.getVisibility()).isEqualTo(adventure.getVisibility().name());
        assertThat(result.getUsersAllowedToRead()).hasSameElementsAs(adventure.getUsersAllowedToRead());
        assertThat(result.getUsersAllowedToWrite()).hasSameElementsAs(adventure.getUsersAllowedToWrite());
        assertThat(result.getCreationDate()).isEqualTo(adventure.getCreationDate());
        assertThat(result.getLastUpdateDate()).isEqualTo(adventure.getLastUpdateDate());
        assertThat(result.getOwnerId()).isEqualTo(adventure.getOwnerId());
        assertThat(result.getGameMode()).isEqualTo(adventure.getGameMode().name());
    }

    @Test
    public void mapAdventureDomain_whenSearchAdventure_thenMapToServer() {

        // Given
        List<Adventure> adventures = IntStream.range(0, 20)
                .mapToObj(op -> AdventureFixture.publicSingleplayerAdventure()
                        .id(String.valueOf(op + 1))
                        .build())
                .toList();

        Pageable pageable = Pageable.ofSize(10);
        Page<Adventure> page = new PageImpl<>(adventures, pageable, 20);

        // When
        SearchAdventuresResult result = mapper.mapToResult(page);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getItems()).isEqualTo(20);
    }
}
