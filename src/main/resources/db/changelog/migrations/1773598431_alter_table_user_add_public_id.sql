--liquibase formatted sql
--changeset moirai:1773598431_alter_table_user_add_public_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE moirai_user ADD COLUMN public_id UUID UNIQUE;

UPDATE moirai_user
   SET public_id = b.uuid
  FROM user_backup b
 WHERE moirai_user.id = b.nano_id;

ALTER TABLE moirai_user ALTER COLUMN public_id SET NOT NULL;

--rollback ALTER TABLE moirai_user DROP COLUMN public_id;
