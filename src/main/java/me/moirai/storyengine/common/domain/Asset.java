package me.moirai.storyengine.common.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import me.moirai.storyengine.common.dbutil.AssetBaseDataAssigner;

@MappedSuperclass
@EntityListeners(AssetBaseDataAssigner.class)
public abstract class Asset {

    @Column(name = "creator_id")
    protected String creatorId;

    @Column(name = "creation_date")
    protected Instant creationDate;

    @Column(name = "last_update_date")
    protected Instant lastUpdateDate;

    @Version
    private int version;

    protected Asset() {
    }

    public String getCreatorId() {
        return creatorId;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public Instant getLastUpdateDate() {
        return lastUpdateDate;
    }

    public int getVersion() {
        return version;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public void setLastUpdateDate(Instant lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
