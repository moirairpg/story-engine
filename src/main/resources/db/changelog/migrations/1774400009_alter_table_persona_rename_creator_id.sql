--liquibase formatted sql
--changeset moirai:1774400009_alter_table_persona_rename_creator_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE persona RENAME COLUMN creator_id TO created_by;

/* liquibase rollback
ALTER TABLE persona RENAME COLUMN created_by TO creator_id;
*/
