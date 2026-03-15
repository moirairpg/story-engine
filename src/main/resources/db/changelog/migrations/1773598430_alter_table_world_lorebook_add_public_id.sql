--liquibase formatted sql
--changeset moirai:1773598430_alter_table_world_lorebook_add_public_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE world_lorebook ADD COLUMN public_id UUID UNIQUE;

UPDATE world_lorebook
   SET public_id = b.uuid
  FROM world_lorebook_backup b
 WHERE world_lorebook.id = b.nano_id;

ALTER TABLE world_lorebook ALTER COLUMN public_id SET NOT NULL;

--rollback ALTER TABLE world_lorebook DROP COLUMN public_id;
