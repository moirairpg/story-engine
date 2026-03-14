package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntry;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateLorebookEntryRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateLorebookEntryRequest;

@ExtendWith(MockitoExtension.class)
public class AdventureLorebookEntryRequestMapperTest {

    @InjectMocks
    private AdventureLorebookEntryRequestMapper mapper;

    @Test
    public void mapCreationRequestToCommand() {

        // Given
        String adventureId = "WRLDID";
        String requesterId = "RQSTRID";

        CreateLorebookEntryRequest request = new CreateLorebookEntryRequest();
        request.setName("Volin Habar");
        request.setDescription("Volin Habar is a warrior that fights with a sword.");
        request.setRegex("[Vv]olin [Hh]abar|[Vv]oha");
        request.setPlayerDiscordId("2423423423423");

        // When
        CreateAdventureLorebookEntry command = mapper.toCommand(request, adventureId, requesterId);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.getName()).isEqualTo(request.getName());
        assertThat(command.getDescription()).isEqualTo(request.getDescription());
        assertThat(command.getRegex()).isEqualTo(request.getRegex());
        assertThat(command.getPlayerId()).isEqualTo(request.getPlayerId());
        assertThat(command.getAdventureId()).isEqualTo(adventureId);
        assertThat(command.getRequesterDiscordId()).isEqualTo(requesterId);
    }

    @Test
    public void mapUpdateRequestToCommand() {

        // Given
        String entryId = "ENTRID";
        String adventureId = "WRLDID";
        String requesterId = "RQSTRID";

        UpdateLorebookEntryRequest request = new UpdateLorebookEntryRequest();
        request.setName("Volin Habar");
        request.setDescription("Volin Habar is a warrior that fights with a sword.");
        request.setRegex("[Vv]olin [Hh]abar|[Vv]oha");
        request.setPlayerDiscordId("2423423423423");
        request.setIsPlayerCharacter(true);

        // When
        UpdateAdventureLorebookEntry command = mapper.toCommand(request, entryId, adventureId, requesterId);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.getId()).isEqualTo(entryId);
        assertThat(command.getAdventureId()).isEqualTo(adventureId);
        assertThat(command.getRequesterDiscordId()).isEqualTo(requesterId);
        assertThat(command.getName()).isEqualTo(request.getName());
        assertThat(command.getDescription()).isEqualTo(request.getDescription());
        assertThat(command.getRegex()).isEqualTo(request.getRegex());
        assertThat(command.getPlayerId()).isEqualTo(request.getPlayerId());
        assertThat(command.getAdventureId()).isEqualTo(adventureId);
        assertThat(command.getRequesterDiscordId()).isEqualTo(requesterId);
    }

    @Test
    public void mapDeleteRequestToCommand() {

        // Given
        String entryId = "ENTRID";
        String adventureId = "WRLDID";
        String requesterId = "RQSTRID";

        // When
        DeleteAdventureLorebookEntry command = mapper.toCommand(entryId, adventureId, requesterId);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.getLorebookEntryId()).isEqualTo(entryId);
        assertThat(command.getAdventureId()).isEqualTo(adventureId);
        assertThat(command.getAdventureId()).isEqualTo(adventureId);
        assertThat(command.getRequesterDiscordId()).isEqualTo(requesterId);
    }
}
