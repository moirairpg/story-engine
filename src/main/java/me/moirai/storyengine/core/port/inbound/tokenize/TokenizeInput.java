package me.moirai.storyengine.core.port.inbound.tokenize;

import me.moirai.storyengine.common.cqs.query.Query;

public record TokenizeInput(String input) implements Query<TokenizeResult> {
}
