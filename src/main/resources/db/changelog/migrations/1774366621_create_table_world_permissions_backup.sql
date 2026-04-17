--liquibase formatted sql
--changeset moirai:1774366621_create_table_world_permissions_backup
--preconditions onFail:HALT, onError:HALT

CREATE TABLE world_permissions_backup (
    world_id               BIGINT,
    owner_id               VARCHAR(100),
    users_allowed_to_read  VARCHAR,
    users_allowed_to_write VARCHAR
);

INSERT INTO world_permissions_backup (world_id, owner_id, users_allowed_to_read, users_allowed_to_write)
SELECT id, owner_id, users_allowed_to_read, users_allowed_to_write FROM world;

--rollback DROP TABLE world_permissions_backup;
