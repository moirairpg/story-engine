--liquibase formatted sql
--changeset moirai:1773598419_create_table_user_backup
--preconditions onFail:HALT, onError:HALT

CREATE TABLE user_backup (
    nano_id    VARCHAR(100),
    uuid       UUID,
    numeric_id BIGINT
);

INSERT INTO user_backup (nano_id, uuid)
     SELECT id,
            gen_random_uuid()
       FROM moirai_user;

--rollback DROP TABLE user_backup;
