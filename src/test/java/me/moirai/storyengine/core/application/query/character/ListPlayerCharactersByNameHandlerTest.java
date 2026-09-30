package me.moirai.storyengine.core.application.query.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.CharacterClass;
import me.moirai.storyengine.core.port.inbound.character.ListPlayerCharactersByName;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterSearchReader;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterSummaryRow;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@ExtendWith(MockitoExtension.class)
public class ListPlayerCharactersByNameHandlerTest {

    @Mock
    private PlayerCharacterSearchReader reader;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private ListPlayerCharactersByNameHandler handler;

    @Test
    void shouldMapMatchingCharacters() {

        // given
        when(reader.listByName(eq("con"), eq(123L))).thenReturn(List.of(playerCharacter()));
        when(storagePort.resolveUrl(any())).thenReturn("http://image.url");

        // when
        var result = handler.execute(new ListPlayerCharactersByName("con", 123L));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isNotNull();
        assertThat(result.get(0).ownerUsername()).isEqualTo("joao.das.couves");
        assertThat(result.get(0).ownerDisplayName()).isEqualTo("João das Couves");
        assertThat(result.get(0).name()).isEqualTo("Conan the Barbarian");
        assertThat(result.get(0).characterClass()).isEqualTo(CharacterClass.BARBARIAN);
        assertThat(result.get(0).background()).isEqualTo("Orphaned.");
        assertThat(result.get(0).imageUrl()).isEqualTo("http://image.url");
    }

    @Test
    void shouldReturnEmptyWhenNoMatches() {

        // given
        when(reader.listByName(any(), eq(123L))).thenReturn(List.of());

        // when
        var result = handler.execute(new ListPlayerCharactersByName("zzz", 123L));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTheSavedImagePositionWhenTheCharacterHasOne() {

        // given
        when(reader.listByName(eq("con"), eq(123L))).thenReturn(List.of(playerCharacter()));

        // when
        var result = handler.execute(new ListPlayerCharactersByName("con", 123L));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).uiImagePositionX()).isEqualTo(0.25);
        assertThat(result.get(0).uiImagePositionY()).isEqualTo(0.75);
    }

    private PlayerCharacterSummaryRow playerCharacter() {
        return new PlayerCharacterSummaryRow(
                UUID.randomUUID(),
                "joao.das.couves",
                "João das Couves",
                "Conan the Barbarian",
                CharacterClass.BARBARIAN,
                "Orphaned.",
                "conan-image-key",
                0.25,
                0.75);
    }
}
