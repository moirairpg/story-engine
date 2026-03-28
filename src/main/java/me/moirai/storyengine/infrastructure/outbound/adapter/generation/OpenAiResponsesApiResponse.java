package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiResponsesApiResponse {

    @JsonProperty("output")
    private List<OpenAiResponsesApiOutput> output;

    @JsonProperty("usage")
    private OpenAiResponsesApiUsage usage;

    public OpenAiResponsesApiResponse() {
    }

    public List<OpenAiResponsesApiOutput> getOutput() {
        return output;
    }

    public OpenAiResponsesApiUsage getUsage() {
        return usage;
    }

    public void setOutput(List<OpenAiResponsesApiOutput> output) {
        this.output = output;
    }

    public void setUsage(OpenAiResponsesApiUsage usage) {
        this.usage = usage;
    }
}
