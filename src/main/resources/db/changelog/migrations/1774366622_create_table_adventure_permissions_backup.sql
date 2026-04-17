--liquibase formatted sql
--changeset moirai:1774366622_create_table_adventure_permissions_backup
--preconditions onFail:HALT, onError:HALT

CREATE TABLE adventure_permissions_backup (
    adventure_id           BIGINT,
    owner_id               VARCHAR(100),
    users_allowed_to_read  VARCHAR,
    users_allowed_to_write VARCHAR
);

INSERT INTO adventure_permissions_backup (adventure_id, owner_id, users_allowed_to_read, users_allowed_to_write)
SELECT id, owner_id, users_allowed_to_read, users_allowed_to_write FROM adventure;

--rollback DROP TABLE adventure_permissions_backup;
