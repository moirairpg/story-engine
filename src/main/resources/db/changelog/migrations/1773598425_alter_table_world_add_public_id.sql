--liquibase formatted sql
--changeset moirai:1773598425_alter_table_world_add_public_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE world ADD COLUMN public_id UUID UNIQUE;

UPDATE world
   SET public_id = b.uuid
  FROM world_backup b
 WHERE world.id = b.nano_id;

ALTER TABLE world ALTER COLUMN public_id SET NOT NULL;

--rollback ALTER TABLE world DROP COLUMN public_id;
