--liquibase formatted sql
--changeset moirai:1774720141_drop_table_notification
--preconditions onFail:HALT, onError:HALT

DROP TABLE notification_read CASCADE;
DROP TABLE notification CASCADE;

/* liquibase rollback
CREATE TABLE notification (
    id VARCHAR(100) PRIMARY KEY,
    message VARCHAR(2000) NOT NULL,
    sender_discord_id VARCHAR(100) NOT NULL,
    receiver_discord_id VARCHAR(100),
    type VARCHAR(50),
    is_global BOOLEAN DEFAULT FALSE,
    is_interactable BOOLEAN DEFAULT FALSE,
    metadata VARCHAR(2000),
    version INT DEFAULT 0 NOT NULL,
    creator_discord_id VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    creation_date TIMESTAMP WITH TIME ZONE,
    last_update_date TIMESTAMP WITH TIME ZONE
);

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
*/
