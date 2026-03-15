--liquibase formatted sql
--changeset moirai:1773598437_update_table_adventure_migrate_public_id
--preconditions onFail:HALT, onError:HALT

UPDATE adventure
   SET world_id = w.public_id::text
  FROM world w
 WHERE adventure.world_id = w.id;

UPDATE adventure
   SET persona_id = p.public_id::text
  FROM persona p
 WHERE adventure.persona_id = p.id;

/* liquibase rollback
UPDATE adventure
   SET world_id = b.nano_id
  FROM world_backup b
 WHERE adventure.world_id = b.uuid::text;

UPDATE adventure
   SET persona_id = b.nano_id
  FROM persona_backup b
 WHERE adventure.persona_id = b.uuid::text;
*/
