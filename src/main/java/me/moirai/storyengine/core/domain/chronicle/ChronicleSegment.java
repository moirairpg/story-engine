package me.moirai.storyengine.core.domain.chronicle;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import me.moirai.storyengine.common.annotation.RandomUuid;
import me.moirai.storyengine.common.domain.Asset;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@Entity
@Table(name = "chronicle_segment")
public class ChronicleSegment extends Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @RandomUuid
    @Column(name = "public_id")
    private UUID publicId;

    @Column(name = "adventure_id")
    private Long adventureId;

    @Column(name = "content")
    private String content;

    protected ChronicleSegment() {
        super();
    }

    private ChronicleSegment(Builder builder) {
        super();
        this.adventureId = builder.adventureId;
        this.content = builder.content;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public Long getAdventureId() {
        return adventureId;
    }

    public String getContent() {
        return content;
    }

    public static final class Builder {

        private Long adventureId;
        private String content;

        private Builder() {
        }

        public Builder adventureId(Long adventureId) {
            this.adventureId = adventureId;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public ChronicleSegment build() {
            if (adventureId == null) {
                throw new BusinessRuleViolationException("Adventure ID is required");
            }

            if (content == null || content.isBlank()) {
                throw new BusinessRuleViolationException("Content is required");
            }

            return new ChronicleSegment(this);
        }
    }
}
