package me.moirai.storyengine.core.port.outbound;

public final class ChatMessage {

    public enum Role {

        SYSTEM,
        ASSISTANT,
        USER;
    }

    private final Role role;
    private final String content;

    public ChatMessage(Role role, String content) {
        this.role = role;
        this.content = content;
    }

    public Role getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public static ChatMessage build(Role role, String content) {

        return new ChatMessage(role, content);
    }
}
