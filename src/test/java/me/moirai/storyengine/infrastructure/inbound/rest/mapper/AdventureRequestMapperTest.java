package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateAdventureRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateAdventureRequestFixture;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateAdventureRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateAdventureRequestFixture;

@ExtendWith(MockitoExtension.class)
public class AdventureRequestMapperTest {

    @InjectMocks
    private AdventureRequestMapper mapper;

    @Test
    public void creationRequestToCommand() {

        // Given
        String requesterId = "RQSTRID";
        CreateAdventureRequest request = CreateAdventureRequestFixture.sample();

        // When
        CreateAdventure command = mapper.toCommand(request, requesterId);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.getName()).isEqualTo(request.getName());
        assertThat(command.getVisibility()).isEqualTo(request.getVisibility());
        assertThat(command.getRequesterDiscordId()).isEqualTo(requesterId);
        assertThat(command.getUsersAllowedToWrite()).hasSameElementsAs(request.getUsersAllowedToWrite());
        assertThat(command.getUsersAllowedToRead()).hasSameElementsAs(request.getUsersAllowedToRead());
    }

    @Test
    public void updateRequestToCommand() {

        // Given
        String adventureId = "WRLDID";
        String requesterId = "RQSTRID";
        UpdateAdventureRequest request = UpdateAdventureRequestFixture.sample();

        // When
        UpdateAdventure command = mapper.toCommand(request, adventureId, requesterId);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.getName()).isEqualTo(request.getName());
        assertThat(command.getAdventureStart()).isEqualTo(request.getAdventureStart());
        assertThat(command.getVisibility()).isEqualTo(request.getVisibility());
        assertThat(command.getRequesterDiscordId()).isEqualTo(requesterId);
        assertThat(command.getUsersAllowedToWriteToAdd()).hasSameElementsAs(request.getUsersAllowedToWriteToAdd());
        assertThat(command.getUsersAllowedToWriteToRemove())
                .hasSameElementsAs(request.getUsersAllowedToWriteToRemove());
        assertThat(command.getUsersAllowedToReadToAdd()).hasSameElementsAs(request.getUsersAllowedToReadToAdd());
        assertThat(command.getUsersAllowedToReadToRemove()).hasSameElementsAs(request.getUsersAllowedToReadToRemove());
    }

    @Test
    public void deleteRequestToCommand() {

        // Given
        String adventureId = "WRLDID";
        String requesterId = "RQSTRID";

        // When
        DeleteAdventure command = mapper.toCommand(adventureId, requesterId);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.getId()).isEqualTo(adventureId);
        assertThat(command.getRequesterDiscordId()).isEqualTo(requesterId);
    }
}
