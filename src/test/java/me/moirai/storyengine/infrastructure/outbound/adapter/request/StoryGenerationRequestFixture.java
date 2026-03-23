package me.moirai.storyengine.infrastructure.outbound.adapter.request;

import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageDataFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationRequest;

public class StoryGenerationRequestFixture {

    public static StoryGenerationRequest create() {

        return new StoryGenerationRequest(
                "BOTID",
                "TestBot",
                "BotNickname",
                "CHNLID",
                "GLDID",
                AdventureFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                "CHAT",
                "Nudge",
                "Author's note",
                "Remember",
                "Bump",
                4,
                ModelConfigurationRequestFixture.gpt4Mini(),
                ModerationConfigurationRequestFixture.withFlags(),
                DiscordMessageDataFixture.messageList(5));
    }
}
