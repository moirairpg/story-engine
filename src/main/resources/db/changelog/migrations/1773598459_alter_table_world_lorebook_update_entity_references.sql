--liquibase formatted sql
--changeset moirai:1773598459_alter_table_world_lorebook_update_entity_references
--preconditions onFail:HALT, onError:HALT

ALTER TABLE world_lorebook ADD COLUMN world_id_new BIGINT;

UPDATE world_lorebook
   SET world_id_new = w.id
  FROM world w
 WHERE world_lorebook.world_id = w.public_id::text;

ALTER TABLE world_lorebook DROP COLUMN world_id;
ALTER TABLE world_lorebook RENAME COLUMN world_id_new TO world_id;
ALTER TABLE world_lorebook ALTER COLUMN world_id SET NOT NULL;

/* liquibase rollback
ALTER TABLE world_lorebook ADD COLUMN world_id_new VARCHAR(100);

UPDATE world_lorebook
   SET world_id_new = b.uuid::text
  FROM world_backup b
 WHERE world_lorebook.world_id = b.numeric_id;

ALTER TABLE world_lorebook DROP COLUMN world_id;
ALTER TABLE world_lorebook RENAME COLUMN world_id_new TO world_id;
ALTER TABLE world_lorebook ALTER COLUMN world_id SET NOT NULL;
*/
