package me.moirai.storyengine.common.security.authorization;

import java.util.Map;
import java.util.UUID;

import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;

public class AuthorizationContext {

    private final MoiraiPrincipal principal;
    private final Map<String, Object> fields;

    public AuthorizationContext(MoiraiPrincipal principal, Map<String, Object> fields) {
        this.principal = principal;
        this.fields = fields;
    }

    public MoiraiPrincipal getPrincipal() {
        return principal;
    }

    public <T> T getFieldAs(String name, Class<T> type) {
        return type.cast(fields.get(name));
    }

    public String getFieldAsString(String name) {
        Object value = fields.get(name);
        return value != null ? value.toString() : null;
    }

    public UUID getFieldAsUuid(String name) {
        Object value = fields.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(value.toString());
    }
}
