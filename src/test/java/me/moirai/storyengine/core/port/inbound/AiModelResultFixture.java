package me.moirai.storyengine.core.port.inbound;

import me.moirai.storyengine.core.port.inbound.model.AiModelResult;

public class AiModelResultFixture {

    public static AiModelResult gpt4Omni() {

        return new AiModelResult(
                "GPT-4 Omni",
                "gpt4-omni",
                "gpt-4o",
                128000);
    }

    public static AiModelResult gpt4Mini() {

        return new AiModelResult(
                "GPT-4 Mini",
                "gpt4-mini",
                "gpt-4o-mini",
                128000);
    }

    public static AiModelResult gpt35turbo() {

        return new AiModelResult(
                "GPT-3.5 Turbo",
                "gpt35-turbo",
                "gpt-3.5-turbo",
                16385);
    }
}
