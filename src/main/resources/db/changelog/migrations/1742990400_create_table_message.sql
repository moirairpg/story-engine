--liquibase formatted sql
--changeset moirai:1742990400_create_table_message
--preconditions onFail:HALT, onError:HALT

CREATE TABLE message (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    public_id        UUID NOT NULL UNIQUE,
    adventure_id     BIGINT NOT NULL,
    role             VARCHAR NOT NULL,
    content          TEXT NOT NULL,
    status           VARCHAR NOT NULL DEFAULT 'ACTIVE',
    created_by       VARCHAR NOT NULL DEFAULT 'SYSTEM',
    creation_date    TIMESTAMP WITH TIME ZONE,
    last_update_date TIMESTAMP WITH TIME ZONE,
    version          INT DEFAULT 0 NOT NULL
);

CREATE INDEX idx_message_adventure_id ON message (adventure_id);

--rollback DROP TABLE message CASCADE;
