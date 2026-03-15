--liquibase formatted sql
--changeset moirai:1773598449_alter_table_adventure_lorebook_add_numeric_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure_lorebook ADD COLUMN numeric_id BIGINT GENERATED ALWAYS AS IDENTITY;

UPDATE adventure_lorebook_backup
   SET numeric_id = t.numeric_id
  FROM adventure_lorebook t
 WHERE adventure_lorebook_backup.nano_id = t.id;

ALTER TABLE adventure_lorebook DROP COLUMN id;
ALTER TABLE adventure_lorebook RENAME COLUMN numeric_id TO id;
ALTER TABLE adventure_lorebook ADD PRIMARY KEY (id);

/* liquibase rollback
ALTER TABLE adventure_lorebook ADD COLUMN nano_id VARCHAR(100);

UPDATE adventure_lorebook
   SET nano_id = b.nano_id
  FROM adventure_lorebook_backup b
 WHERE adventure_lorebook.public_id = b.uuid;

ALTER TABLE adventure_lorebook DROP COLUMN id;
ALTER TABLE adventure_lorebook RENAME COLUMN nano_id TO id;
ALTER TABLE adventure_lorebook ADD PRIMARY KEY (id);
*/
