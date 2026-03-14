package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.world.request.CreateWorld;
import me.moirai.storyengine.core.application.usecase.world.request.UpdateWorld;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateWorldRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateWorldRequestFixture;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateWorldRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateWorldRequestFixture;

@ExtendWith(MockitoExtension.class)
public class WorldRequestMapperTest {

    @InjectMocks
    private WorldRequestMapper mapper;

    @Test
    public void creationRequestToCommand() {

        // Given
        String requesterId = "RQSTRID";
        CreateWorldRequest request = CreateWorldRequestFixture.createPrivateWorld();

        // When
        CreateWorld command = mapper.toCommand(request, requesterId);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.getName()).isEqualTo(request.getName());
        assertThat(command.getDescription()).isEqualTo(request.getDescription());
        assertThat(command.getAdventureStart()).isEqualTo(request.getAdventureStart());
        assertThat(command.getVisibility()).isEqualTo(request.getVisibility());
        assertThat(command.getRequesterDiscordId()).isEqualTo(requesterId);
        assertThat(command.getUsersAllowedToWrite()).hasSameElementsAs(request.getUsersAllowedToWrite());
        assertThat(command.getUsersAllowedToRead()).hasSameElementsAs(request.getUsersAllowedToRead());
    }

    @Test
    public void updateRequestToCommand() {

        // Given
        String worldId = "WRLDID";
        String requesterId = "RQSTRID";
        UpdateWorldRequest request = UpdateWorldRequestFixture.createPrivateWorld();

        // When
        UpdateWorld command = mapper.toCommand(request, worldId, requesterId);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.getName()).isEqualTo(request.getName());
        assertThat(command.getDescription()).isEqualTo(request.getDescription());
        assertThat(command.getAdventureStart()).isEqualTo(request.getAdventureStart());
        assertThat(command.getVisibility()).isEqualTo(request.getVisibility());
        assertThat(command.getUsersAllowedToWriteToAdd()).hasSameElementsAs(request.getUsersAllowedToWriteToAdd());
        assertThat(command.getUsersAllowedToWriteToRemove())
                .hasSameElementsAs(request.getUsersAllowedToWriteToRemove());
        assertThat(command.getUsersAllowedToReadToAdd()).hasSameElementsAs(request.getUsersAllowedToReadToAdd());
        assertThat(command.getUsersAllowedToReadToRemove()).hasSameElementsAs(request.getUsersAllowedToReadToRemove());
    }
}
