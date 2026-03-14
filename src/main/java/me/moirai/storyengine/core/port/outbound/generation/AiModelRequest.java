package me.moirai.storyengine.core.port.outbound.generation;

public class AiModelRequest {

    private final String internalModelName;
    private final String officialModelName;
    private final int hardTokenLimit;

    private AiModelRequest(String internalModelName, String officialModelName, int hardTokenLimit) {
        this.internalModelName = internalModelName;
        this.officialModelName = officialModelName;
        this.hardTokenLimit = hardTokenLimit;
    }

    public static AiModelRequest build(String internalModelName, String officialModelName, int hardTokenLimit) {
        return new AiModelRequest(internalModelName, officialModelName, hardTokenLimit);
    }

    public String getInternalModelName() {
        return internalModelName;
    }

    public String getOfficialModelName() {
        return officialModelName;
    }

    public int getHardTokenLimit() {
        return hardTokenLimit;
    }
}
