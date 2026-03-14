package me.moirai.storyengine.infrastructure.outbound.adapter.request;

public class ChatMessage {

    private final String role;
    private final String content;

    private ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static ChatMessage build(String role, String content) {

        return new ChatMessage(role, content);
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}
