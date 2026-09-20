package me.moirai.storyengine.core.domain.userdetails;

import java.util.ArrayList;
import java.util.List;
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
import jakarta.persistence.Transient;
import me.moirai.storyengine.common.domain.Asset;
import me.moirai.storyengine.common.domain.DomainEvent;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@Entity
@Table(name = "moirai_user")
public class User extends Asset {

    private static final int MAX_BIO_LENGTH = 2000;
    private static final String BIO_TOO_LONG = "Bio cannot be longer than 2000 characters";
    private static final String CANNOT_CHANGE_OWN_ROLE = "A user cannot change their own role";
    private static final String CANNOT_CHANGE_OWN_ACTIVE_STATE = "A user cannot change their own active state";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id")
    private UUID publicId;

    @Column(name = "discord_id")
    private String discordId;

    @Column(name = "username")
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "bio")
    private String bio;

    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();

    public User(Builder builder) {

        super();

        this.publicId = Generators.timeBasedEpochGenerator().generate();
        this.discordId = builder.discordId;
        this.username = builder.username;
        this.role = builder.role;
        this.isActive = true;
    }

    protected User() {
        super();
    }

    public static Builder builder() {

        return new Builder();
    }

    public List<DomainEvent> drainEvents() {

        var snapshot = List.copyOf(domainEvents);
        domainEvents.clear();

        return snapshot;
    }

    public void communicateUserDeleted() {
        domainEvents.add(new UserDeletedEvent(this.id, this.publicId, this.username));
    }

    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public String getDiscordId() {
        return discordId;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getBio() {
        return bio;
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public void updateRole(Role role, UUID requesterId) {

        if (role != this.role && publicId.equals(requesterId)) {
            throw new BusinessRuleViolationException(CANNOT_CHANGE_OWN_ROLE);
        }

        this.role = role;
    }

    public void updateActiveState(boolean isActive, UUID requesterId) {

        if (isActive != this.isActive && publicId.equals(requesterId)) {
            throw new BusinessRuleViolationException(CANNOT_CHANGE_OWN_ACTIVE_STATE);
        }

        this.isActive = isActive;
    }

    public void updateBio(String bio) {

        if (bio != null && bio.length() > MAX_BIO_LENGTH) {
            throw new BusinessRuleViolationException(BIO_TOO_LONG);
        }

        this.bio = bio;
    }

    public static final class Builder {

        private String discordId;
        private String username;
        private Role role;

        private Builder() {
        }

        public Builder discordId(String discordId) {

            this.discordId = discordId;
            return this;
        }

        public Builder username(String username) {

            this.username = username;
            return this;
        }

        public Builder role(Role role) {

            this.role = role;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
