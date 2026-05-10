package me.moirai.storyengine.core.application.query.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.CharacterClass;
import me.moirai.storyengine.core.port.inbound.character.SearchPlayerCharacters;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterSearchReader;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterSummaryRow;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@ExtendWith(MockitoExtension.class)
public class SearchPlayerCharactersHandlerTest {

    @Mock
    private PlayerCharacterSearchReader reader;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private SearchPlayerCharactersHandler handler;

    @Test
    void shouldMapResult() {

        // given
        when(reader.search(any(SearchPlayerCharacters.class))).thenReturn(getPages(1, 10, 1));
        when(storagePort.resolveUrl(any())).thenReturn("http://image.url");

        // when
        var result = handler.execute(new SearchPlayerCharacters(
                null,
                null,
                null,
                null,
                1,
                10,
                123L));

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(10);
        assertThat(result.data().get(0).id()).isNotNull();
        assertThat(result.data().get(0).ownerUsername()).isEqualTo("joao.das.couves");
        assertThat(result.data().get(0).name()).isEqualTo("Conan the Barbarian");
        assertThat(result.data().get(0).characterClass()).isEqualTo(CharacterClass.BARBARIAN);
        assertThat(result.data().get(0).imageUrl()).isEqualTo("http://image.url");
    }

    private PlayerCharacterSummaryRow playerCharacter() {
        return new PlayerCharacterSummaryRow(
                UUID.randomUUID(),
                "joao.das.couves",
                "Conan the Barbarian",
                CharacterClass.BARBARIAN,
                "conan-image-key");
    }

    private PaginatedResult<PlayerCharacterSummaryRow> getPages(int page, int amountOfResults, int pages) {

        var characters = new ArrayList<PlayerCharacterSummaryRow>();
        for (int i = 0; i < amountOfResults; i++) {
            characters.add(playerCharacter());
        }

        return PaginatedResult.of(characters, amountOfResults, page, 10);
    }
}
