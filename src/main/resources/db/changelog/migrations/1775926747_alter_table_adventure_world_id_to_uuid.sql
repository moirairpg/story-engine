--liquibase formatted sql
--changeset moirai:1775926747_alter_table_adventure_world_id_to_uuid
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure ADD COLUMN world_id_new UUID;

UPDATE adventure
   SET world_id_new = w.public_id
  FROM world w
 WHERE adventure.world_id = w.id;

ALTER TABLE adventure DROP COLUMN world_id;
ALTER TABLE adventure RENAME COLUMN world_id_new TO world_id;

CREATE INDEX idx_adventure_world_id ON adventure(world_id);

/* liquibase rollback
ALTER TABLE adventure ADD COLUMN world_id_new BIGINT;

UPDATE adventure
   SET world_id_new = w.id
  FROM world w
 WHERE adventure.world_id = w.public_id;

ALTER TABLE adventure DROP COLUMN world_id;
ALTER TABLE adventure RENAME COLUMN world_id_new TO world_id;
ALTER TABLE adventure ALTER COLUMN world_id SET NOT NULL;

DROP INDEX idx_adventure_world_id;
*/
