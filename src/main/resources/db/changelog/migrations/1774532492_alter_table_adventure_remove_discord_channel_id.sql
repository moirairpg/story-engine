--liquibase formatted sql
--changeset moirai:1774532492_alter_table_adventure_remove_discord_channel_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure_backup ADD COLUMN channel_id VARCHAR(100);

UPDATE adventure_backup ab
    SET channel_id = a.channel_id
FROM adventure a
WHERE ab.numeric_id = a.id;

ALTER TABLE adventure DROP COLUMN channel_id;

/* liquibase rollback
ALTER TABLE adventure ADD COLUMN channel_id VARCHAR(100);
UPDATE adventure a SET channel_id = ab.channel_id FROM adventure_backup ab WHERE a.id = ab.numeric_id;
ALTER TABLE adventure_backup DROP COLUMN channel_id;
*/