--liquibase formatted sql
--changeset moirai:1774315884_create_table_adventure_logit_bias
--preconditions onFail:HALT, onError:HALT

CREATE TABLE adventure_logit_bias (
    adventure_id BIGINT  NOT NULL REFERENCES adventure(id) ON DELETE CASCADE,
    token_id     VARCHAR NOT NULL,
    bias         NUMERIC NOT NULL
);

/* liquibase rollback
DROP TABLE adventure_logit_bias
*/
