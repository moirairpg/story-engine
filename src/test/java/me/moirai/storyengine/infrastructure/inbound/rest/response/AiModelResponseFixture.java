package me.moirai.storyengine.infrastructure.inbound.rest.response;

public class AiModelResponseFixture {

    public static AiModelResponse.Builder gpt4Omni() {

        return AiModelResponse.builder()
                .fullModelName("GPT-4 Omni")
                .internalModelName("gpt4-omni")
                .officialModelName("gpt-4o")
                .hardTokenLimit(128000);
    }

    public static AiModelResponse.Builder gpt4Mini() {

        return AiModelResponse.builder()
                .fullModelName("GPT-4 Mini")
                .internalModelName("gpt4-mini")
                .officialModelName("gpt-4o-mini")
                .hardTokenLimit(128000);
    }

    public static AiModelResponse.Builder gpt35turbo() {

        return AiModelResponse.builder()
                .fullModelName("GPT-3.5 Turbo")
                .internalModelName("gpt35-turbo")
                .officialModelName("gpt-3.5-turbo")
                .hardTokenLimit(16385);
    }
}
