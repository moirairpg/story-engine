--liquibase formatted sql
--changeset moirai:1774400008_alter_table_world_rename_creator_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE world RENAME COLUMN creator_id TO created_by;

/* liquibase rollback
ALTER TABLE world RENAME COLUMN created_by TO creator_id;
*/
