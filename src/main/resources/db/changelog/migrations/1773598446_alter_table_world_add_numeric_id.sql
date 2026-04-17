--liquibase formatted sql
--changeset moirai:1773598446_alter_table_world_add_numeric_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE world ADD COLUMN numeric_id BIGINT GENERATED ALWAYS AS IDENTITY;

UPDATE world_backup
   SET numeric_id = t.numeric_id
  FROM world t
 WHERE world_backup.nano_id = t.id;

ALTER TABLE world DROP COLUMN id;
ALTER TABLE world RENAME COLUMN numeric_id TO id;
ALTER TABLE world ADD PRIMARY KEY (id);

/* liquibase rollback
ALTER TABLE world ADD COLUMN nano_id VARCHAR(100);

UPDATE world
   SET nano_id = b.nano_id
  FROM world_backup b
 WHERE world.public_id = b.uuid;

ALTER TABLE world DROP COLUMN id;
ALTER TABLE world RENAME COLUMN nano_id TO id;
ALTER TABLE world ADD PRIMARY KEY (id);
*/
