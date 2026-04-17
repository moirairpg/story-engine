--liquibase formatted sql
--changeset moirai:1774731362_alter_table_adventure_drop_model_configuration_extended_fields
--preconditions onFail:HALT, onError:HALT

DROP TABLE adventure_stop_sequences;
DROP TABLE adventure_logit_bias;
ALTER TABLE adventure DROP COLUMN frequency_penalty;
ALTER TABLE adventure DROP COLUMN presence_penalty;

/* liquibase rollback
ALTER TABLE adventure ADD COLUMN presence_penalty NUMERIC DEFAULT 0 NOT NULL;
ALTER TABLE adventure ADD COLUMN frequency_penalty NUMERIC DEFAULT 0 NOT NULL;
CREATE TABLE adventure_logit_bias (
    adventure_id BIGINT  NOT NULL REFERENCES adventure(id) ON DELETE CASCADE,
    token_id     VARCHAR NOT NULL,
    bias         NUMERIC NOT NULL
);
CREATE TABLE adventure_stop_sequences (
    adventure_id BIGINT  NOT NULL REFERENCES adventure(id) ON DELETE CASCADE,
    value        VARCHAR NOT NULL
);
*/
