package me.moirai.storyengine.infrastructure.security.authentication;

public enum MoiraiCookie {

    SESSION_COOKIE("moirai_sstk", true),
    REFRESH_COOKIE("moirai_reftk", true),
    EXPIRY_COOKIE("moirai_expiry", false);

    private final String name;
    private final boolean isHttpOnly;

    private MoiraiCookie(String name, boolean isHttpOnly) {
        this.name = name;
        this.isHttpOnly = isHttpOnly;
    }

    public String getName() {
        return name;
    }

    public boolean isHttpOnly() {
        return isHttpOnly;
    }
}
