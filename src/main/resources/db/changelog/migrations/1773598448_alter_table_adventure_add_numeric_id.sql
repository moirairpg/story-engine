--liquibase formatted sql
--changeset moirai:1773598448_alter_table_adventure_add_numeric_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure ADD COLUMN numeric_id BIGINT GENERATED ALWAYS AS IDENTITY;

UPDATE adventure_backup
   SET numeric_id = t.numeric_id
  FROM adventure t
 WHERE adventure_backup.nano_id = t.id;

ALTER TABLE adventure DROP COLUMN id;
ALTER TABLE adventure RENAME COLUMN numeric_id TO id;
ALTER TABLE adventure ADD PRIMARY KEY (id);

/* liquibase rollback
ALTER TABLE adventure ADD COLUMN nano_id VARCHAR(100);

UPDATE adventure
   SET nano_id = b.nano_id
  FROM adventure_backup b
 WHERE adventure.public_id = b.uuid;

ALTER TABLE adventure DROP COLUMN id;
ALTER TABLE adventure RENAME COLUMN nano_id TO id;
ALTER TABLE adventure ADD PRIMARY KEY (id);
*/
