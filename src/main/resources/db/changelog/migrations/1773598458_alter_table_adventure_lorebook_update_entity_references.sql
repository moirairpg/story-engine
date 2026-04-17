--liquibase formatted sql
--changeset moirai:1773598458_alter_table_adventure_lorebook_update_entity_references
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure_lorebook ADD COLUMN adventure_id_new BIGINT;

UPDATE adventure_lorebook
   SET adventure_id_new = a.id
  FROM adventure a
 WHERE adventure_lorebook.adventure_id = a.public_id::text;

ALTER TABLE adventure_lorebook DROP COLUMN adventure_id;
ALTER TABLE adventure_lorebook RENAME COLUMN adventure_id_new TO adventure_id;
ALTER TABLE adventure_lorebook ALTER COLUMN adventure_id SET NOT NULL;

/* liquibase rollback
ALTER TABLE adventure_lorebook ADD COLUMN adventure_id_new VARCHAR(100);

UPDATE adventure_lorebook
   SET adventure_id_new = b.uuid::text
  FROM adventure_backup b
 WHERE adventure_lorebook.adventure_id = b.numeric_id;

ALTER TABLE adventure_lorebook DROP COLUMN adventure_id;
ALTER TABLE adventure_lorebook RENAME COLUMN adventure_id_new TO adventure_id;
ALTER TABLE adventure_lorebook ALTER COLUMN adventure_id SET NOT NULL;
*/
