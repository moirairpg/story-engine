package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiResponsesApiOutput {

    @JsonProperty("content")
    private List<OpenAiResponsesApiOutputContent> content;

    public OpenAiResponsesApiOutput() {
    }

    public List<OpenAiResponsesApiOutputContent> getContent() {
        return content;
    }

    public void setContent(List<OpenAiResponsesApiOutputContent> content) {
        this.content = content;
    }
}
