package me.moirai.storyengine.infrastructure.outbound.adapter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.IntArrayList;
import com.knuddels.jtokkit.api.ModelType;

import me.moirai.storyengine.core.application.usecase.discord.slashcommands.TokenizeResult;
import me.moirai.storyengine.core.domain.port.TokenizerPort;

@Component
public class TokenizerAdapter implements TokenizerPort {

    private static final String TOKEN_DELIMITER = "|";
    private final Encoding encoding;

    public TokenizerAdapter() {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        this.encoding = registry.getEncodingForModel(ModelType.GPT_4);
    }

    @Override
    public int[] getTokensIdsFrom(String text) {
        if (StringUtils.isBlank(text)) {
            return new int[0];
        }
        IntArrayList ids = encoding.encode(text);
        int[] result = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            result[i] = ids.get(i);
        }
        return result;
    }

    @Override
    public int getTokenCountFrom(String text) {
        if (StringUtils.isBlank(text)) {
            return 0;
        }
        return encoding.countTokens(text);
    }

    @Override
    public String getTokens(String text) {
        int[] ids = getTokensIdsFrom(text);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                sb.append(TOKEN_DELIMITER);
            }
            sb.append(ids[i]);
        }
        return sb.toString();
    }

    @Override
    public TokenizeResult tokenize(String text) {
        String tokens = getTokens(text);
        int[] tokenIds = getTokensIdsFrom(text);
        int tokenCount = tokenIds.length;
        int characterCount = text.length();

        return TokenizeResult.builder()
                .characterCount(characterCount)
                .tokenCount(tokenCount)
                .tokens(tokens)
                .tokenIds(tokenIds)
                .build();
    }
}
