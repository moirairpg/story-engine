--liquibase formatted sql
--changeset moirai:1774366625_alter_table_world_drop_permission_columns
--preconditions onFail:HALT, onError:HALT

ALTER TABLE world DROP COLUMN IF EXISTS owner_id;
ALTER TABLE world DROP COLUMN IF EXISTS users_allowed_to_read;
ALTER TABLE world DROP COLUMN IF EXISTS users_allowed_to_write;

/* liquibase rollback
ALTER TABLE world ADD COLUMN owner_id VARCHAR(100);
ALTER TABLE world ADD COLUMN users_allowed_to_read VARCHAR;
ALTER TABLE world ADD COLUMN users_allowed_to_write VARCHAR;
UPDATE world w SET owner_id = b.owner_id, users_allowed_to_read = b.users_allowed_to_read, users_allowed_to_write = b.users_allowed_to_write
FROM world_permissions_backup b WHERE b.world_id = w.id;
*/
