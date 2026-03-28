--liquibase formatted sql
--changeset moirai:1774366619_create_table_adventure_permissions
--preconditions onFail:HALT, onError:HALT

CREATE TABLE adventure_permissions (
    adventure_id BIGINT  NOT NULL REFERENCES adventure(id) ON DELETE CASCADE,
    user_id      BIGINT  NOT NULL,
    permission   VARCHAR NOT NULL
);

/* liquibase rollback
DROP TABLE adventure_permissions
*/
