--liquibase formatted sql
--changeset moirai:1773598416_create_table_adventure_backup
--preconditions onFail:HALT, onError:HALT

CREATE TABLE adventure_backup (
    nano_id    VARCHAR(100),
    uuid       UUID,
    numeric_id BIGINT
);

INSERT INTO adventure_backup (nano_id, uuid)
     SELECT id,
            gen_random_uuid()
       FROM adventure;

--rollback DROP TABLE adventure_backup;
