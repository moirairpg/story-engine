package me.moirai.storyengine.common.enums;

import java.util.Arrays;

import me.moirai.storyengine.common.exception.AIModelNotSupportedException;

public enum ArtificialIntelligenceModel {

    GPT54("GPT-5.4", "gpt-5.4", 1050000),
    GPT54_MINI("GPT-5.4 Mini", "gpt-5.4-mini", 400000),
    GPT54_NANO("GPT-5.4 Nano", "gpt-5.4-nano", 400000);

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
