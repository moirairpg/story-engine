--liquibase formatted sql
--changeset moirai:1773598439_update_table_world_lorebook_migrate_public_id
--preconditions onFail:HALT, onError:HALT

UPDATE world_lorebook
   SET world_id = w.public_id::text
  FROM world w
 WHERE world_lorebook.world_id = w.id;

/* liquibase rollback
UPDATE world_lorebook
   SET world_id = b.nano_id
  FROM world_backup b
 WHERE world_lorebook.world_id = b.uuid::text;
*/
