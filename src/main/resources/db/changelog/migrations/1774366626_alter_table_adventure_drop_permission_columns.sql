--liquibase formatted sql
--changeset moirai:1774366626_alter_table_adventure_drop_permission_columns
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure DROP COLUMN IF EXISTS owner_id;
ALTER TABLE adventure DROP COLUMN IF EXISTS users_allowed_to_read;
ALTER TABLE adventure DROP COLUMN IF EXISTS users_allowed_to_write;

/* liquibase rollback
ALTER TABLE adventure ADD COLUMN owner_id VARCHAR(100);
ALTER TABLE adventure ADD COLUMN users_allowed_to_read VARCHAR;
ALTER TABLE adventure ADD COLUMN users_allowed_to_write VARCHAR;
UPDATE adventure a SET owner_id = b.owner_id, users_allowed_to_read = b.users_allowed_to_read, users_allowed_to_write = b.users_allowed_to_write
FROM adventure_permissions_backup b WHERE b.adventure_id = a.id;
*/
