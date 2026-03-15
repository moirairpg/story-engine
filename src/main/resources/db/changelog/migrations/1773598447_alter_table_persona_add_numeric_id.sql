--liquibase formatted sql
--changeset moirai:1773598447_alter_table_persona_add_numeric_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE persona ADD COLUMN numeric_id BIGINT GENERATED ALWAYS AS IDENTITY;

UPDATE persona_backup
   SET numeric_id = t.numeric_id
  FROM persona t
 WHERE persona_backup.nano_id = t.id;

ALTER TABLE persona DROP COLUMN id;
ALTER TABLE persona RENAME COLUMN numeric_id TO id;
ALTER TABLE persona ADD PRIMARY KEY (id);

/* liquibase rollback
ALTER TABLE persona ADD COLUMN nano_id VARCHAR(100);

UPDATE persona
   SET nano_id = b.nano_id
  FROM persona_backup b
 WHERE persona.public_id = b.uuid;

ALTER TABLE persona DROP COLUMN id;
ALTER TABLE persona RENAME COLUMN nano_id TO id;
ALTER TABLE persona ADD PRIMARY KEY (id);
*/
