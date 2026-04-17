--liquibase formatted sql
--changeset moirai:1773598429_alter_table_adventure_lorebook_add_public_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure_lorebook ADD COLUMN public_id UUID UNIQUE;

UPDATE adventure_lorebook
   SET public_id = b.uuid
  FROM adventure_lorebook_backup b
 WHERE adventure_lorebook.id = b.nano_id;

ALTER TABLE adventure_lorebook ALTER COLUMN public_id SET NOT NULL;

--rollback ALTER TABLE adventure_lorebook DROP COLUMN public_id;
