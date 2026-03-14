package me.moirai.storyengine.infrastructure.outbound.adapter.request;

public final class DiscordEmbeddedMessageRequest {

    public static class Color {

        public static final Color GREEN = new Color(0, 255, 0);
        public static final Color BLUE = new Color(0, 0, 255);
        public static final Color RED = new Color(255, 0, 0);
        public static final Color WHITE = new Color(255, 255, 255);
        public static final Color YELLOW = new Color(255, 255, 0);

        private int red;
        private int green;
        private int blue;

        private Color(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public int getRed() {
            return red;
        }

        public int getGreen() {
            return green;
        }

        public int getBlue() {
            return blue;
        }

        public static Color customColor(int red, int green, int blue) {
            return new Color(red, green, blue);
        }
    }

    private final String authorName;
    private final String authorWebsiteUrl;
    private final String authorIconUrl;
    private final String messageContent;
    private final String imageUrl;
    private final Color embedColor;
    private final String titleText;
    private final String footerText;
    private final String thumbnailUrl;

    public DiscordEmbeddedMessageRequest(Builder builder) {
        this.authorName = builder.authorName;
        this.authorWebsiteUrl = builder.authorWebsiteUrl;
        this.authorIconUrl = builder.authorIconUrl;
        this.messageContent = builder.messageContent;
        this.imageUrl = builder.imageUrl;
        this.embedColor = builder.embedColor;
        this.titleText = builder.titleText;
        this.footerText = builder.footerText;
        this.thumbnailUrl = builder.thumbnailUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorWebsiteUrl() {
        return authorWebsiteUrl;
    }

    public String getAuthorIconUrl() {
        return authorIconUrl;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Color getEmbedColor() {
        return embedColor;
    }

    public String getTitleText() {
        return titleText;
    }

    public String getFooterText() {
        return footerText;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public static final class Builder {

        private String authorName;
        private String authorWebsiteUrl;
        private String authorIconUrl;
        private String messageContent;
        private String imageUrl;
        private Color embedColor;
        private String titleText;
        private String footerText;
        private String thumbnailUrl;

        public Builder authorName(String authorName) {
            this.authorName = authorName;
            return this;
        }

        public Builder authorWebsiteUrl(String authorWebsiteUrl) {
            this.authorWebsiteUrl = authorWebsiteUrl;
            return this;
        }

        public Builder authorIconUrl(String authorIconUrl) {
            this.authorIconUrl = authorIconUrl;
            return this;
        }

        public Builder messageContent(String messageContent) {
            this.messageContent = messageContent;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder embedColor(Color embedColor) {

            if (embedColor == null) {
                this.embedColor = Color.WHITE;
                return this;
            }

            this.embedColor = embedColor;
            return this;
        }

        public Builder titleText(String titleText) {
            this.titleText = titleText;
            return this;
        }

        public Builder footerText(String footerText) {
            this.footerText = footerText;
            return this;
        }

        public Builder thumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
            return this;
        }

        public DiscordEmbeddedMessageRequest build() {
            return new DiscordEmbeddedMessageRequest(this);
        }
    }
}
