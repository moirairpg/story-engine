--liquibase formatted sql
--changeset moirai:1773598428_alter_table_adventure_add_public_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure ADD COLUMN public_id UUID UNIQUE;

UPDATE adventure
   SET public_id = b.uuid
  FROM adventure_backup b
 WHERE adventure.id = b.nano_id;

ALTER TABLE adventure ALTER COLUMN public_id SET NOT NULL;

--rollback ALTER TABLE adventure DROP COLUMN public_id;
