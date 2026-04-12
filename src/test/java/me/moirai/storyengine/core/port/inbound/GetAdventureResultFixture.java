package me.moirai.storyengine.core.port.inbound;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;

public class GetAdventureResultFixture {

    public static AdventureDetails privateMultiplayerAdventure() {

        var adventure = AdventureFixture.privateMultiplayerAdventureWithIdAndPermissions();

        var modelConfiguration = new ModelConfigurationDto(
                adventure.getModelConfiguration().getAiModel(),
                adventure.getModelConfiguration().getMaxTokenLimit(),
                adventure.getModelConfiguration().getTemperature());

        var contextAttributes = new ContextAttributesDto(
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().scene(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency());

        var permissions = adventure.getPermissions().stream()
                .map(permission -> new PermissionDto(
                        UUID.fromString("d6622c6c-85bb-41ba-aa53-93fa68681f85"),
                        PermissionLevel.OWNER))
                .collect(Collectors.toSet());

        return new AdventureDetails(
                adventure.getPublicId(),
                adventure.getName(),
                adventure.getDescription(),
                adventure.getAdventureStart(),
                WorldFixture.PUBLIC_ID,
                adventure.getNarratorName(),
                adventure.getNarratorPersonality(),
                adventure.getVisibility(),
                adventure.getModeration(),
                adventure.isMultiplayer(),
                adventure.getCreationDate(),
                adventure.getLastUpdateDate(),
                modelConfiguration,
                contextAttributes,
                permissions,
                Set.of());
    }

    public static AdventureDetails publicMultiplayerAdventure() {

        var adventure = AdventureFixture.publicMultiplayerAdventureWithIdAndPermissions();

        var modelConfiguration = new ModelConfigurationDto(
                adventure.getModelConfiguration().getAiModel(),
                adventure.getModelConfiguration().getMaxTokenLimit(),
                adventure.getModelConfiguration().getTemperature());

        var contextAttributes = new ContextAttributesDto(
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().scene(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency());

        var permissions = adventure.getPermissions().stream()
                .map(permission -> new PermissionDto(
                        UUID.fromString("d6622c6c-85bb-41ba-aa53-93fa68681f85"),
                        PermissionLevel.OWNER))
                .collect(Collectors.toSet());

        return new AdventureDetails(
                adventure.getPublicId(),
                adventure.getName(),
                adventure.getDescription(),
                adventure.getAdventureStart(),
                WorldFixture.PUBLIC_ID,
                adventure.getNarratorName(),
                adventure.getNarratorPersonality(),
                adventure.getVisibility(),
                adventure.getModeration(),
                adventure.isMultiplayer(),
                adventure.getCreationDate(),
                adventure.getLastUpdateDate(),
                modelConfiguration,
                contextAttributes,
                permissions,
                Set.of());
    }
}
