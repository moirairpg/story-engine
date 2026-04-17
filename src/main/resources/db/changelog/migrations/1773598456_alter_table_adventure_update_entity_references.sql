--liquibase formatted sql
--changeset moirai:1773598456_alter_table_adventure_update_entity_references
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure ADD COLUMN world_id_new BIGINT;

UPDATE adventure
   SET world_id_new = w.id
  FROM world w
 WHERE adventure.world_id = w.public_id::text;

ALTER TABLE adventure DROP COLUMN world_id;
ALTER TABLE adventure RENAME COLUMN world_id_new TO world_id;

ALTER TABLE adventure ADD COLUMN persona_id_new BIGINT;

UPDATE adventure
   SET persona_id_new = p.id
  FROM persona p
 WHERE adventure.persona_id = p.public_id::text;

ALTER TABLE adventure DROP COLUMN persona_id;
ALTER TABLE adventure RENAME COLUMN persona_id_new TO persona_id;
ALTER TABLE adventure ALTER COLUMN persona_id SET NOT NULL;

/* liquibase rollback
ALTER TABLE adventure ADD COLUMN world_id_new VARCHAR(100);

UPDATE adventure
   SET world_id_new = b.uuid::text
  FROM world_backup b
 WHERE adventure.world_id = b.numeric_id;

ALTER TABLE adventure DROP COLUMN world_id;
ALTER TABLE adventure RENAME COLUMN world_id_new TO world_id;
ALTER TABLE adventure ALTER COLUMN world_id SET NOT NULL;

ALTER TABLE adventure ADD COLUMN persona_id_new VARCHAR(100);

UPDATE adventure
   SET persona_id_new = b.uuid::text
  FROM persona_backup b
 WHERE adventure.persona_id = b.numeric_id;

ALTER TABLE adventure DROP COLUMN persona_id;
ALTER TABLE adventure RENAME COLUMN persona_id_new TO persona_id;
ALTER TABLE adventure ALTER COLUMN persona_id SET NOT NULL;
*/
