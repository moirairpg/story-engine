--liquibase formatted sql
--changeset moirai:1773598418_create_table_world_lorebook_backup
--preconditions onFail:HALT, onError:HALT

CREATE TABLE world_lorebook_backup (
    nano_id    VARCHAR(100),
    uuid       UUID,
    numeric_id BIGINT
);

INSERT INTO world_lorebook_backup (nano_id, uuid)
     SELECT id,
            gen_random_uuid()
       FROM world_lorebook;

--rollback DROP TABLE world_lorebook_backup;
