--liquibase formatted sql
--changeset moirai:1773598438_update_table_adventure_lorebook_migrate_public_id
--preconditions onFail:HALT, onError:HALT

UPDATE adventure_lorebook
   SET adventure_id = a.public_id::text
  FROM adventure a
 WHERE adventure_lorebook.adventure_id = a.id;

/* liquibase rollback
UPDATE adventure_lorebook
   SET adventure_id = b.nano_id
  FROM adventure_backup b
 WHERE adventure_lorebook.adventure_id = b.uuid::text;
*/
