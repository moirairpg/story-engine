package me.moirai.storyengine.common.domain;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@MappedSuperclass
public abstract class ShareableAsset extends Asset {

    protected abstract List<Permission> permissions();

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    private Visibility visibility;

    protected ShareableAsset(Visibility visibility) {
        super();
        this.visibility = visibility;
    }

    protected ShareableAsset() {
        super();
    }

    public boolean isOwner(Long userId) {
        return permissions().stream()
                .anyMatch(p -> p.userId().equals(userId) && p.level() == PermissionLevel.OWNER);
    }

    public void grant(Permission permission) {
        if (permissions().stream()
                .anyMatch(p -> p.userId().equals(permission.userId()) && p.level() == PermissionLevel.OWNER)) {
            throw new BusinessRuleViolationException("Owner permission cannot be overwritten");
        }

        permissions().removeIf(p -> p.userId().equals(permission.userId()));
        permissions().add(permission);
    }

    public void revoke(Long userId) {
        if (permissions().stream().anyMatch(p -> p.userId().equals(userId) && p.level() == PermissionLevel.OWNER)) {
            throw new BusinessRuleViolationException("Owner permission cannot be revoked");
        }

        permissions().removeIf(p -> p.userId().equals(userId));
    }

    public void updatePermissions(Set<Permission> newPermissions) {
        var owner = permissions().stream()
                .filter(p -> p.level() == PermissionLevel.OWNER)
                .findFirst()
                .orElseThrow();

        permissions().clear();
        permissions().add(owner);

        newPermissions.stream()
                .filter(p -> p.level() != PermissionLevel.OWNER)
                .forEach(permissions()::add);
    }

    public boolean canWrite(Long userId) {
        return permissions().stream()
                .anyMatch(p -> p.userId().equals(userId)
                        && (p.level() == PermissionLevel.WRITE || p.level() == PermissionLevel.OWNER));
    }

    public boolean canRead(Long userId) {
        return permissions().stream()
                .anyMatch(p -> p.userId().equals(userId)
                        && (p.level() == PermissionLevel.READ || p.level() == PermissionLevel.WRITE
                                || p.level() == PermissionLevel.OWNER));
    }

    public List<Permission> getPermissions() {
        return Collections.unmodifiableList(permissions());
    }

    public boolean isPublic() {
        return visibility.equals(Visibility.PUBLIC);
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
}
