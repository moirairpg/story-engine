--liquibase formatted sql
--changeset moirai:1773598450_alter_table_world_lorebook_add_numeric_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE world_lorebook ADD COLUMN numeric_id BIGINT GENERATED ALWAYS AS IDENTITY;

UPDATE world_lorebook_backup
   SET numeric_id = t.numeric_id
  FROM world_lorebook t
 WHERE world_lorebook_backup.nano_id = t.id;

ALTER TABLE world_lorebook DROP COLUMN id;
ALTER TABLE world_lorebook RENAME COLUMN numeric_id TO id;
ALTER TABLE world_lorebook ADD PRIMARY KEY (id);

/* liquibase rollback
ALTER TABLE world_lorebook ADD COLUMN nano_id VARCHAR(100);

UPDATE world_lorebook
   SET nano_id = b.nano_id
  FROM world_lorebook_backup b
 WHERE world_lorebook.public_id = b.uuid;

ALTER TABLE world_lorebook DROP COLUMN id;
ALTER TABLE world_lorebook RENAME COLUMN nano_id TO id;
ALTER TABLE world_lorebook ADD PRIMARY KEY (id);
*/
