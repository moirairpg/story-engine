--liquibase formatted sql
--changeset moirai:1740331206_create_table_notification
--preconditions onFail:HALT, onError:HALT

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

--rollback DROP TABLE notification CASCADE;
