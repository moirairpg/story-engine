--liquibase formatted sql
--changeset moirai:1776807616_create_table_notification_read
--preconditions onFail:HALT, onError:HALT

CREATE TABLE notification_read (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    notification_id BIGINT NOT NULL REFERENCES notification(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    read_date TIMESTAMPTZ(6)
);

/* liquibase rollback
DROP TABLE notification_read
*/
