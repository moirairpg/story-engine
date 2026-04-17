--liquibase formatted sql
--changeset moirai:1774366625_alter_table_persona_drop_permission_columns
--preconditions onFail:HALT, onError:HALT

ALTER TABLE persona DROP COLUMN IF EXISTS owner_id;
ALTER TABLE persona DROP COLUMN IF EXISTS users_allowed_to_read;
ALTER TABLE persona DROP COLUMN IF EXISTS users_allowed_to_write;

/* liquibase rollback
ALTER TABLE persona ADD COLUMN owner_id VARCHAR(100);
ALTER TABLE persona ADD COLUMN users_allowed_to_read VARCHAR;
ALTER TABLE persona ADD COLUMN users_allowed_to_write VARCHAR;
UPDATE persona p SET owner_id = b.owner_id, users_allowed_to_read = b.users_allowed_to_read, users_allowed_to_write = b.users_allowed_to_write
FROM persona_permissions_backup b WHERE b.persona_id = p.id;
*/
