--liquibase formatted sql
--changeset moirai:1776807608_create_table_notification
--preconditions onFail:HALT, onError:HALT

CREATE TABLE notification (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    message VARCHAR(2000) NOT NULL,
    type VARCHAR(50) NOT NULL,
    level VARCHAR(50),
    adventure_id BIGINT,
    is_interactable BOOLEAN DEFAULT FALSE,
    metadata JSONB,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    creation_date TIMESTAMPTZ(6),
    last_update_date TIMESTAMPTZ(6),
    version INT DEFAULT 0 NOT NULL
);

CREATE INDEX idx_notification_adventure ON notification(adventure_id);

/* liquibase rollback
DROP INDEX idx_notification_adventure;
DROP TABLE notification;
*/
