--liquibase formatted sql
--changeset moirai:1775947831_drop_table_persona
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure DROP COLUMN persona_id;
DROP TABLE persona CASCADE;

/* liquibase rollback
ALTER TABLE adventure ADD COLUMN persona_id BIGINT;

CREATE TABLE persona (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    personality VARCHAR NOT NULL,
    nudge_content VARCHAR,
    nudge_role VARCHAR(25),
    bump_content VARCHAR,
    bump_role VARCHAR(25),
    bump_frequency SMALLINT,
    owner_discord_id VARCHAR(100) NOT NULL,
    discord_users_allowed_to_read VARCHAR,
    discord_users_allowed_to_write VARCHAR,
    visibility VARCHAR(20) NOT NULL,
    version INT DEFAULT 0 NOT NULL,
    creator_discord_id VARCHAR(100) NOT NULL,
    creation_date TIMESTAMP WITH TIME ZONE,
    last_update_date TIMESTAMP WITH TIME ZONE
);

CREATE TABLE persona_permissions (
    persona_id  BIGINT  NOT NULL REFERENCES persona(id) ON DELETE CASCADE,
    user_id     BIGINT  NOT NULL,
    permission  VARCHAR NOT NULL
);
*/
