--liquibase formatted sql
--changeset moirai:1773546062_drop_table_favorite
--preconditions onFail:HALT, onError:HALT

DROP TABLE favorite CASCADE;

/* liquibase rollback
CREATE TABLE favorite (
    id VARCHAR(100) PRIMARY KEY,
    player_discord_id VARCHAR(100) NOT NULL,
    asset_id VARCHAR(100) NOT NULL,
    asset_type VARCHAR(20) NOT NULL
);
*/