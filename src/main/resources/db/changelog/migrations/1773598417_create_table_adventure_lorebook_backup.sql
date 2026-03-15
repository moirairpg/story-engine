--liquibase formatted sql
--changeset moirai:1773598417_create_table_adventure_lorebook_backup
--preconditions onFail:HALT, onError:HALT

CREATE TABLE adventure_lorebook_backup (
    nano_id    VARCHAR(100),
    uuid       UUID,
    numeric_id BIGINT
);

INSERT INTO adventure_lorebook_backup (nano_id, uuid)
     SELECT id,
            gen_random_uuid()
       FROM adventure_lorebook;

--rollback DROP TABLE adventure_lorebook_backup;
