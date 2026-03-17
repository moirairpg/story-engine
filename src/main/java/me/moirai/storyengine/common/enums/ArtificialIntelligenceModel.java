package me.moirai.storyengine.common.enums;

import java.util.Arrays;

import me.moirai.storyengine.common.exception.AIModelNotSupportedException;

public enum ArtificialIntelligenceModel {

    GPT35_TURBO("GPT-3.5 Turbo", "gpt-3.5-turbo", 16385),
    GPT4_MINI("GPT-4 Mini", "gpt-4o-mini", 128000),
    GPT4_OMNI("GPT-4 Omni", "gpt-4o", 128000);

    private final String fullModelName;
    private final String officialModelName;
    private final int hardTokenLimit;

    private ArtificialIntelligenceModel(
            String fullModelName,
            String officialModelName,
            int hardTokenLimit) {

        this.fullModelName = fullModelName;
        this.officialModelName = officialModelName;
        this.hardTokenLimit = hardTokenLimit;
    }

    public String getFullModelName() {
        return fullModelName;
    }

    public String getOfficialModelName() {
        return officialModelName;
    }

    public int getHardTokenLimit() {
        return hardTokenLimit;
    }

    @Override
    public String toString() {

        return this.name();
    }

    public static ArtificialIntelligenceModel fromString(String modelToSearch) {

        return Arrays.stream(values())
                .filter(aiModel -> aiModel.name().equals(modelToSearch.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new AIModelNotSupportedException("Unsupported model: " + modelToSearch));
    }
}
