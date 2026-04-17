--liquibase formatted sql
--changeset moirai:1775947829_alter_table_adventure_world_add_narrator_columns
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure ADD COLUMN narrator_name VARCHAR(100);
ALTER TABLE adventure ADD COLUMN narrator_personality TEXT;

ALTER TABLE world ADD COLUMN narrator_name VARCHAR(100);
ALTER TABLE world ADD COLUMN narrator_personality TEXT;

--rollback ALTER TABLE world DROP COLUMN narrator_personality;
--rollback ALTER TABLE world DROP COLUMN narrator_name;
--rollback ALTER TABLE adventure DROP COLUMN narrator_personality;
--rollback ALTER TABLE adventure DROP COLUMN narrator_name;
