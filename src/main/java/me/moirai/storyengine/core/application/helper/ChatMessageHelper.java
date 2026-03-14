package me.moirai.storyengine.core.application.helper;

import java.util.Map;

public interface ChatMessageHelper {

    Map<String, Object> addMessagesToContext(Map<String, Object> context, int reservedTokens);

    Map<String, Object> addMessagesToContext(Map<String, Object> context, int reservedTokens, int amountOfMessage);

    Map<String, Object> addMessagesToContext(Map<String, Object> context, int reservedTokens, String assetManipulated);
}
