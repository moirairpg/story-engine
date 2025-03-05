--liquibase formatted sql
--changeset moirai:1740331494_create_table_notification_read
--preconditions onFail:HALT, onError:HALT

CREATE TABLE notification_read (
    id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    read_at TIMESTAMP WITH TIME ZONE,
    notification_id VARCHAR(100) REFERENCES notification(id),
    version INT DEFAULT 0 NOT NULL,
    creator_discord_id VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    creation_date TIMESTAMP WITH TIME ZONE,
    last_update_date TIMESTAMP WITH TIME ZONE
);

--rollback DROP TABLE notification_read CASCADE;