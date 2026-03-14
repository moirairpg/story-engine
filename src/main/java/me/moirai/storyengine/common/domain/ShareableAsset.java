package me.moirai.storyengine.common.domain;

import static java.util.Collections.unmodifiableSet;

import java.time.OffsetDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ShareableAsset extends Asset {

    @Embedded
    private Permissions permissions;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    private Visibility visibility;

    protected ShareableAsset(String creatorId, OffsetDateTime creationDate,
            OffsetDateTime lastUpdateDate, Permissions permissions, Visibility visibility, int version) {

        super(creatorId, creationDate, lastUpdateDate, version);
        this.permissions = permissions;
        this.visibility = visibility;
    }

    protected ShareableAsset() {
        super();
    }

    public boolean isPublic() {

        return this.visibility.equals(Visibility.PUBLIC);
    }

    public void makePublic() {

        this.visibility = Visibility.PUBLIC;
    }

    public void makePrivate() {

        this.visibility = Visibility.PRIVATE;
    }

    public Visibility getVisibility() {

        return visibility;
    }

    public boolean isOwner(String discordUserId) {

        return permissions.getOwnerId().equals(discordUserId);
    }

    public boolean canUserWrite(String discordUserId) {

        boolean isWriter = permissions.getUsersAllowedToWrite().contains(discordUserId);

        return isOwner(discordUserId) || isWriter;
    }

    public boolean canUserRead(String discordUserId) {

        boolean isReader = permissions.getUsersAllowedToRead().contains(discordUserId);

        return canUserWrite(discordUserId) || isReader || isPublic();
    }

    public void addWriterUser(String discordUserId) {

        Permissions newPermissions = this.permissions
                .allowUserToWrite(discordUserId, this.permissions.getOwnerId());

        this.permissions = newPermissions;
    }

    public void addReaderUser(String discordUserId) {

        Permissions newPermissions = this.permissions
                .allowUserToRead(discordUserId, this.permissions.getOwnerId());

        this.permissions = newPermissions;
    }

    public void removeWriterUser(String discordUserId) {

        Permissions newPermissions = this.permissions
                .disallowUserToWrite(discordUserId, this.permissions.getOwnerId());

        this.permissions = newPermissions;
    }

    public void removeReaderUser(String discordUserId) {

        Permissions newPermissions = this.permissions
                .disallowUserToRead(discordUserId, this.permissions.getOwnerId());

        this.permissions = newPermissions;
    }

    public Set<String> getUsersAllowedToWrite() {

        return unmodifiableSet(this.permissions.getUsersAllowedToWrite());
    }

    public Set<String> getUsersAllowedToRead() {

        return unmodifiableSet(this.permissions.getUsersAllowedToRead());
    }

    public String getOwnerId() {

        return this.permissions.getOwnerId();
    }
}
