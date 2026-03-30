--liquibase formatted sql
--changeset moirai:1774836776_create_table_chronicle_segment
--preconditions onFail:HALT, onError:HALT

CREATE TABLE chronicle_segment (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    public_id        UUID NOT NULL UNIQUE,
    adventure_id     BIGINT NOT NULL,
    content          TEXT NOT NULL,
    created_by       VARCHAR NOT NULL DEFAULT 'SYSTEM',
    creation_date    TIMESTAMP WITH TIME ZONE,
    last_update_date TIMESTAMP WITH TIME ZONE,
    version          INT DEFAULT 0 NOT NULL
);

CREATE INDEX idx_chronicle_segment_adventure_id_creation_date
    ON chronicle_segment (adventure_id, creation_date);

--rollback DROP TABLE chronicle_segment CASCADE;
