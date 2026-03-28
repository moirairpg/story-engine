package me.moirai.storyengine.infrastructure.inbound.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AdventureWebSocketHandler adventureWebSocketHandler;

    public WebSocketConfig(AdventureWebSocketHandler adventureWebSocketHandler) {
        this.adventureWebSocketHandler = adventureWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(adventureWebSocketHandler, "/ws/adventure/{adventureId}")
                .setAllowedOriginPatterns("*");
    }
}
