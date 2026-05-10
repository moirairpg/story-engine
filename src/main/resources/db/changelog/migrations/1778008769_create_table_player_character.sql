--liquibase formatted sql
--changeset moirai:1778008769_create_table_player_character
--preconditions onFail:HALT, onError:HALT

CREATE TABLE player_character (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    player_id BIGINT NOT NULL,
    personality VARCHAR NOT NULL,
    physical_description VARCHAR NOT NULL,
    character_class VARCHAR(20) NOT NULL,
    image_key VARCHAR,
    version INT DEFAULT 0 NOT NULL,
    created_by VARCHAR(100),
    creation_date TIMESTAMPTZ(6),
    last_update_date TIMESTAMPTZ(6)
);

CREATE INDEX idx_player_character_player_id ON player_character(player_id);

/* liquibase rollback
DROP TABLE player_character;
*/