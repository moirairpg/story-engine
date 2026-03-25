--liquibase formatted sql
--changeset moirai:1774400013_alter_table_moirai_user_rename_creator_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE moirai_user RENAME COLUMN creator_id TO created_by;

/* liquibase rollback
ALTER TABLE moirai_user RENAME COLUMN created_by TO creator_id;
*/
