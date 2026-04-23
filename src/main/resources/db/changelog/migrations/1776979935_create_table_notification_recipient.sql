--liquibase formatted sql
--changeset moirai:1776979935_create_table_notification_recipient
--preconditions onFail:HALT, onError:HALT

CREATE TABLE notification_recipient (
    notification_id BIGINT NOT NULL REFERENCES notification(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (notification_id, user_id)
);

CREATE INDEX idx_notification_recipient_user ON notification_recipient(user_id);

/* liquibase rollback
DROP INDEX idx_notification_recipient_user;
DROP TABLE notification_recipient;
*/
