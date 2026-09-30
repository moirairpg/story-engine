package me.moirai.storyengine.core.application.query.adventure;

import static me.moirai.storyengine.common.enums.Moderation.STRICT;
import static me.moirai.storyengine.common.enums.Visibility.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.common.enums.CharacterClass;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureById;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureDetailsRow;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureReader;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRosterReader;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureMembershipSummaryRow;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@ExtendWith(MockitoExtension.class)
public class GetAdventureByIdHandlerTest {

    private static final UUID REQUESTER_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID OTHER_USER_ID = UUID.fromString("99999999-8888-7777-6666-555555555555");

    @Mock
    private AdventureReader reader;

    @Mock
    private AdventureRosterReader adventureRosterReader;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private GetAdventureByIdHandler handler;

    @Test
    public void shouldThrowExceptionWhenQueryIsNull() {

        // Given
        GetAdventureById query = null;

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void shouldThrowExceptionWhenAdventureIsNotFound() {

        // Given
        var query = new GetAdventureById(AdventureFixture.PUBLIC_ID, REQUESTER_ID);

        when(reader.getAdventureById(any(UUID.class))).thenReturn(Optional.empty());

        // Then
        assertThrows(NotFoundException.class, () -> handler.handle(query));
    }

    @Test
    public void shouldReturnAdventureDetailsWhenAdventureIsFound() {

        // Given
        var modelConfiguration = new ModelConfigurationDto(
                ArtificialIntelligenceModel.GPT54_MINI, 2048, 1.0);

        var contextAttributes = new ContextAttributesDto(null, null, null, null, 0);

        var expectedDetails = new AdventureDetailsRow(
                AdventureFixture.PUBLIC_ID,
                "Name",
                "desc",
                "start",
                WorldFixture.PUBLIC_ID,
                "Aria",
                "A helpful guide",
                PRIVATE,
                STRICT,
                true,
                null,
                null,
                null,
                modelConfiguration,
                contextAttributes,
                Set.of(),
                Set.of(),
                null,
                null);

        var query = new GetAdventureById(AdventureFixture.PUBLIC_ID, REQUESTER_ID);

        when(reader.getAdventureById(any(UUID.class))).thenReturn(Optional.of(expectedDetails));
        when(adventureRosterReader.getAllByAdventurePublicId(any(UUID.class))).thenReturn(List.of());

        // When
        AdventureDetails result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(AdventureFixture.PUBLIC_ID);
        assertThat(result.worldId()).isEqualTo(WorldFixture.PUBLIC_ID);
        assertThat(result.narratorName()).isEqualTo("Aria");
        assertThat(result.narratorPersonality()).isEqualTo("A helpful guide");
        assertThat(result.rpgMechanicsEnabled()).isTrue();
    }

    @Test
    public void shouldResolveCharacterImageUrlsWhenRosterHasCharacters() {

        // Given
        var modelConfiguration = new ModelConfigurationDto(
                ArtificialIntelligenceModel.GPT54_MINI, 2048, 1.0);

        var contextAttributes = new ContextAttributesDto(null, null, null, null, 0);

        var expectedDetails = new AdventureDetailsRow(
                AdventureFixture.PUBLIC_ID,
                "Name",
                "desc",
                "start",
                WorldFixture.PUBLIC_ID,
                "Aria",
                "A helpful guide",
                PRIVATE,
                STRICT,
                true,
                null,
                null,
                null,
                modelConfiguration,
                contextAttributes,
                Set.of(),
                Set.of(),
                null,
                null);

        var characterId = UUID.randomUUID();
        var playerId = UUID.randomUUID();

        var rosterRow = new AdventureMembershipSummaryRow(
                characterId,
                playerId,
                "john.doe",
                "Johnny the Bold",
                "Volin Habar",
                CharacterClass.PALADIN,
                "characters/image-key.png",
                0.25,
                0.75);

        var query = new GetAdventureById(AdventureFixture.PUBLIC_ID, REQUESTER_ID);

        when(reader.getAdventureById(any(UUID.class))).thenReturn(Optional.of(expectedDetails));
        when(adventureRosterReader.getAllByAdventurePublicId(any(UUID.class))).thenReturn(List.of(rosterRow));
        when(storagePort.resolveUrl("characters/image-key.png")).thenReturn("http://image.url");

        // When
        AdventureDetails result = handler.handle(query);

        // Then
        assertThat(result.roster()).hasSize(1);
        assertThat(result.roster().getFirst().playerCharacterId()).isEqualTo(characterId);
        assertThat(result.roster().getFirst().playerUsername()).isEqualTo("john.doe");
        assertThat(result.roster().getFirst().playerDisplayName()).isEqualTo("Johnny the Bold");
        assertThat(result.roster().getFirst().characterClass()).isEqualTo(CharacterClass.PALADIN);
        assertThat(result.roster().getFirst().imageUrl()).isEqualTo("http://image.url");
        assertThat(result.roster().getFirst().uiImagePositionX()).isEqualTo(0.25);
        assertThat(result.roster().getFirst().uiImagePositionY()).isEqualTo(0.75);
    }

    @Test
    public void shouldReturnBothFlagsAsTrueWhenRequesterIsTheOwner() {

        // given
        var row = adventureRowWith(Set.of(new PermissionDto(REQUESTER_ID, PermissionLevel.OWNER)));
        var query = new GetAdventureById(AdventureFixture.PUBLIC_ID, REQUESTER_ID);

        when(reader.getAdventureById(any(UUID.class))).thenReturn(Optional.of(row));
        when(adventureRosterReader.getAllByAdventurePublicId(any(UUID.class))).thenReturn(List.of());

        // when
        var result = handler.handle(query);

        // then
        assertThat(result.canManage()).isTrue();
        assertThat(result.isOwner()).isTrue();
    }

    @Test
    public void shouldReturnCanManageWithoutOwnershipWhenRequesterHasWritePermission() {

        // given
        var row = adventureRowWith(Set.of(
                new PermissionDto(OTHER_USER_ID, PermissionLevel.OWNER),
                new PermissionDto(REQUESTER_ID, PermissionLevel.WRITE)));

        var query = new GetAdventureById(AdventureFixture.PUBLIC_ID, REQUESTER_ID);

        when(reader.getAdventureById(any(UUID.class))).thenReturn(Optional.of(row));
        when(adventureRosterReader.getAllByAdventurePublicId(any(UUID.class))).thenReturn(List.of());

        // when
        var result = handler.handle(query);

        // then
        assertThat(result.canManage()).isTrue();
        assertThat(result.isOwner()).isFalse();
    }

    @Test
    public void shouldReturnBothFlagsAsFalseWhenRequesterHasReadPermission() {

        // given
        var row = adventureRowWith(Set.of(
                new PermissionDto(OTHER_USER_ID, PermissionLevel.OWNER),
                new PermissionDto(REQUESTER_ID, PermissionLevel.READ)));

        var query = new GetAdventureById(AdventureFixture.PUBLIC_ID, REQUESTER_ID);

        when(reader.getAdventureById(any(UUID.class))).thenReturn(Optional.of(row));
        when(adventureRosterReader.getAllByAdventurePublicId(any(UUID.class))).thenReturn(List.of());

        // when
        var result = handler.handle(query);

        // then
        assertThat(result.canManage()).isFalse();
        assertThat(result.isOwner()).isFalse();
    }

    @Test
    public void shouldReturnBothFlagsAsFalseWhenRequesterHasNoPermissionEntry() {

        // given
        var row = adventureRowWith(Set.of(new PermissionDto(OTHER_USER_ID, PermissionLevel.OWNER)));
        var query = new GetAdventureById(AdventureFixture.PUBLIC_ID, REQUESTER_ID);

        when(reader.getAdventureById(any(UUID.class))).thenReturn(Optional.of(row));
        when(adventureRosterReader.getAllByAdventurePublicId(any(UUID.class))).thenReturn(List.of());

        // when
        var result = handler.handle(query);

        // then
        assertThat(result.canManage()).isFalse();
        assertThat(result.isOwner()).isFalse();
    }

    private AdventureDetailsRow adventureRowWith(Set<PermissionDto> permissions) {

        var modelConfiguration = new ModelConfigurationDto(
                ArtificialIntelligenceModel.GPT54_MINI, 2048, 1.0);

        var contextAttributes = new ContextAttributesDto(null, null, null, null, 0);

        return new AdventureDetailsRow(
                AdventureFixture.PUBLIC_ID,
                "Name",
                "desc",
                "start",
                WorldFixture.PUBLIC_ID,
                "Aria",
                "A helpful guide",
                PRIVATE,
                STRICT,
                true,
                null,
                null,
                null,
                modelConfiguration,
                contextAttributes,
                permissions,
                Set.of(),
                null,
                null);
    }
}
