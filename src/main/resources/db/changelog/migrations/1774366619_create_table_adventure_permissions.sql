--liquibase formatted sql
--changeset moirai:1774366619_create_table_adventure_permissions
--preconditions onFail:HALT, onError:HALT

CREATE TABLE adventure_permissions (
    adventure_id BIGINT  NOT NULL REFERENCES adventure(id) ON DELETE CASCADE,
    user_id      BIGINT  NOT NULL,
    permission   VARCHAR NOT NULL,
    CONSTRAINT uq_adventure_permissions UNIQUE (adventure_id, user_id)
);

/* liquibase rollback
DROP TABLE adventure_permissions
*/
