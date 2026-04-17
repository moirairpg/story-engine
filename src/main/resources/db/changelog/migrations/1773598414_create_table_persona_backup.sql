--liquibase formatted sql
--changeset moirai:1773598414_create_table_persona_backup
--preconditions onFail:HALT, onError:HALT

CREATE TABLE persona_backup (
    nano_id    VARCHAR(100),
    uuid       UUID,
    numeric_id BIGINT
);

INSERT INTO persona_backup (nano_id, uuid)
     SELECT id,
            gen_random_uuid()
       FROM persona;

--rollback DROP TABLE persona_backup;
