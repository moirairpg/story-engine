--liquibase formatted sql
--changeset moirai:1774366608_create_table_persona_permissions
--preconditions onFail:HALT, onError:HALT

CREATE TABLE persona_permissions (
    persona_id  BIGINT  NOT NULL REFERENCES persona(id) ON DELETE CASCADE,
    user_id     BIGINT  NOT NULL,
    permission  VARCHAR NOT NULL,
    CONSTRAINT uq_persona_permissions UNIQUE (persona_id, user_id)
);

/* liquibase rollback
DROP TABLE persona_permissions
*/
