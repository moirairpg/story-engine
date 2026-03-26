--liquibase formatted sql
--changeset moirai:1774532482_alter_table_adventure_remove_game_mode
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure_backup ADD COLUMN game_mode VARCHAR(10);

UPDATE adventure_backup ab
    SET game_mode = a.game_mode
FROM adventure a
WHERE ab.numeric_id = a.id;

ALTER TABLE adventure DROP COLUMN game_mode;

/* liquibase rollback
ALTER TABLE adventure ADD COLUMN game_mode VARCHAR(10);
UPDATE adventure a SET game_mode = ab.game_mode FROM adventure_backup ab WHERE a.id = ab.numeric_id;
ALTER TABLE adventure_backup DROP COLUMN game_mode;
*/