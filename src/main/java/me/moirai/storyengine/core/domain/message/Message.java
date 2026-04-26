package me.moirai.storyengine.core.domain.message;

import java.util.UUID;

import com.fasterxml.uuid.Generators;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import me.moirai.storyengine.common.domain.Asset;
import me.moirai.storyengine.common.enums.MessageAuthorRole;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@Entity
@Table(name = "message")
public class Message extends Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id")
    private UUID publicId;

    @Column(name = "adventure_id")
    private Long adventureId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private MessageAuthorRole role;

    @Column(name = "content")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MessageStatus status;

    protected Message() {
        super();
    }

    private Message(Builder builder) {

        super();

        this.publicId = Generators.timeBasedEpochGenerator().generate();
        this.adventureId = builder.adventureId;
        this.role = builder.role;
        this.content = builder.content;
        this.status = builder.status;
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

    public MessageAuthorRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void markAsChronicled() {
        this.status = MessageStatus.CHRONICLED;
    }

    public static final class Builder {

        private Long adventureId;
        private MessageAuthorRole role;
        private String content;
        private MessageStatus status = MessageStatus.ACTIVE;

        private Builder() {
        }

        public Builder adventureId(Long adventureId) {
            this.adventureId = adventureId;
            return this;
        }

        public Builder role(MessageAuthorRole role) {
            this.role = role;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder status(MessageStatus status) {
            this.status = status;
            return this;
        }

        public Message build() {
            if (adventureId == null) {
                throw new BusinessRuleViolationException("Adventure ID is required");
            }

            if (role == null) {
                throw new BusinessRuleViolationException("Role is required");
            }

            if (content == null || content.isBlank()) {
                throw new BusinessRuleViolationException("Content is required");
            }

            return new Message(this);
        }
    }
}
