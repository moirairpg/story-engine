--liquibase formatted sql
--changeset moirai:1774315886_alter_table_adventure_drop_stop_sequences_and_logit_bias
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure DROP COLUMN stop_sequences;
ALTER TABLE adventure DROP COLUMN logit_bias;

/* liquibase rollback
ALTER TABLE adventure ADD COLUMN stop_sequences VARCHAR;
ALTER TABLE adventure ADD COLUMN logit_bias VARCHAR;
*/
