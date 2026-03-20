package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRawDto;

@ExtendWith(MockitoExtension.class)
public class AdventurePersistenceMapperTest {

    @InjectMocks
    private AdventurePersistenceMapper mapper;

    @Test
    public void mapAdventureDomain_whenGetOperation_thenMapToGetResult() {

        var adventure = AdventureFixture.publicSingleplayerAdventure().build();

        AdventureRawDto result = mapper.mapToResult(adventure);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(adventure.getPublicId());
        assertThat(result.name()).isEqualTo(adventure.getName());
        assertThat(result.visibility()).isEqualTo(adventure.getVisibility().name());
        assertThat(result.usersAllowedToRead()).hasSameElementsAs(adventure.getUsersAllowedToRead());
        assertThat(result.usersAllowedToWrite()).hasSameElementsAs(adventure.getUsersAllowedToWrite());
        assertThat(result.creationDate()).isEqualTo(adventure.getCreationDate());
        assertThat(result.lastUpdateDate()).isEqualTo(adventure.getLastUpdateDate());
        assertThat(result.ownerId()).isEqualTo(adventure.getOwnerId());
        assertThat(result.gameMode()).isEqualTo(adventure.getGameMode().name());
        assertThat(result.personaId()).isEqualTo(adventure.getPersonaId());
    }
}
