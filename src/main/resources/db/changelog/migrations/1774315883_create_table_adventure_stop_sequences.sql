--liquibase formatted sql
--changeset moirai:1774315883_create_table_adventure_stop_sequences
--preconditions onFail:HALT, onError:HALT

CREATE TABLE adventure_stop_sequences (
    adventure_id BIGINT  NOT NULL REFERENCES adventure(id) ON DELETE CASCADE,
    value        VARCHAR NOT NULL
);

/* liquibase rollback
DROP TABLE adventure_stop_sequences
*/
