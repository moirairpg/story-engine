package me.moirai.storyengine.core.application.usecase.model.result;

import me.moirai.storyengine.core.port.inbound.model.AiModelResult;

public class AiModelResultFixture {

    public static AiModelResult.Builder gpt4Omni() {

        return AiModelResult.builder()
                .fullModelName("GPT-4 Omni")
                .internalModelName("gpt4-omni")
                .officialModelName("gpt-4o")
                .hardTokenLimit(128000);
    }

    public static AiModelResult.Builder gpt4Mini() {

        return AiModelResult.builder()
                .fullModelName("GPT-4 Mini")
                .internalModelName("gpt4-mini")
                .officialModelName("gpt-4o-mini")
                .hardTokenLimit(128000);
    }

    public static AiModelResult.Builder gpt35turbo() {

        return AiModelResult.builder()
                .fullModelName("GPT-3.5 Turbo")
                .internalModelName("gpt35-turbo")
                .officialModelName("gpt-3.5-turbo")
                .hardTokenLimit(16385);
    }
}
