--liquibase formatted sql
--changeset moirai:1773598413_create_table_world_backup
--preconditions onFail:HALT, onError:HALT

CREATE TABLE world_backup (
    nano_id    VARCHAR(100),
    uuid       UUID,
    numeric_id BIGINT
);

INSERT INTO world_backup (nano_id, uuid)
     SELECT id,
            gen_random_uuid()
       FROM world;

--rollback DROP TABLE world_backup;
