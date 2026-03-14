package me.moirai.storyengine.common.domain;

import java.time.OffsetDateTime;

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

    @Column(name = "creation_date", nullable = false)
    protected OffsetDateTime creationDate;

    @Column(name = "last_update_date", nullable = false)
    protected OffsetDateTime lastUpdateDate;

    @Version
    private int version;

    protected Asset(String creatorId,
            OffsetDateTime creationDate,
            OffsetDateTime lastUpdateDate,
            int version) {

        this.creatorId = creatorId;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.version = version;
    }

    protected Asset() {
        super();
    }

    public String getCreatorId() {
        return creatorId;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public OffsetDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public int getVersion() {
        return version;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public void setCreationDate(OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public void setLastUpdateDate(OffsetDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
