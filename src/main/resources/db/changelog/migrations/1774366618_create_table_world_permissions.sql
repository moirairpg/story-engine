--liquibase formatted sql
--changeset moirai:1774366618_create_table_world_permissions
--preconditions onFail:HALT, onError:HALT

CREATE TABLE world_permissions (
    world_id    BIGINT  NOT NULL REFERENCES world(id) ON DELETE CASCADE,
    user_id     BIGINT  NOT NULL,
    permission  VARCHAR NOT NULL
);

/* liquibase rollback
DROP TABLE world_permissions
*/
