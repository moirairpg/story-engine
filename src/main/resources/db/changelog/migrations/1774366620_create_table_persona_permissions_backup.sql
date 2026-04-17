--liquibase formatted sql
--changeset moirai:1774366620_create_table_persona_permissions_backup
--preconditions onFail:HALT, onError:HALT

CREATE TABLE persona_permissions_backup (
    persona_id             BIGINT,
    owner_id               VARCHAR(100),
    users_allowed_to_read  VARCHAR,
    users_allowed_to_write VARCHAR
);

INSERT INTO persona_permissions_backup (persona_id, owner_id, users_allowed_to_read, users_allowed_to_write)
SELECT id, owner_id, users_allowed_to_read, users_allowed_to_write FROM persona;

--rollback DROP TABLE persona_permissions_backup;
