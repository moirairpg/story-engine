package me.moirai.storyengine.common.security.authentication;

import org.springframework.security.core.context.SecurityContextHolder;

public class MoiraiSecurityContext {

    private static final ThreadLocal<MoiraiPrincipal> holder = new ThreadLocal<>();

    private MoiraiSecurityContext() {
    }

    public static MoiraiPrincipal getAuthenticatedUser() {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return (MoiraiPrincipal) auth.getPrincipal();
        }

        return holder.get();
    }

    public static void set(MoiraiPrincipal principal) {
        holder.set(principal);
    }

    public static void clear() {
        holder.remove();
    }
}
