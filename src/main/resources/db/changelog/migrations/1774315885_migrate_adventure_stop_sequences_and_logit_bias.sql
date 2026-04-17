--liquibase formatted sql
--changeset moirai:1774315885_migrate_adventure_stop_sequences_and_logit_bias
--preconditions onFail:HALT, onError:HALT

INSERT INTO adventure_stop_sequences (adventure_id, value)
SELECT a.id, trim(s.value)
FROM adventure a,
     unnest(string_to_array(a.stop_sequences, ',')) AS s(value)
WHERE a.stop_sequences IS NOT NULL AND a.stop_sequences <> '';

INSERT INTO adventure_logit_bias (adventure_id, token_id, bias)
SELECT a.id, split_part(entry, '=', 1), split_part(entry, '=', 2)::NUMERIC
FROM adventure a,
     unnest(string_to_array(a.logit_bias, ',')) AS entry
WHERE a.logit_bias IS NOT NULL AND a.logit_bias <> '';

/* liquibase rollback
DELETE FROM adventure_stop_sequences;
DELETE FROM adventure_logit_bias;
*/
