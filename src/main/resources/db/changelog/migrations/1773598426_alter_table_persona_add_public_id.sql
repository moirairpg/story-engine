--liquibase formatted sql
--changeset moirai:1773598426_alter_table_persona_add_public_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE persona ADD COLUMN public_id UUID UNIQUE;

UPDATE persona
   SET public_id = b.uuid
  FROM persona_backup b
 WHERE persona.id = b.nano_id;

ALTER TABLE persona ALTER COLUMN public_id SET NOT NULL;

--rollback ALTER TABLE persona DROP COLUMN public_id;
