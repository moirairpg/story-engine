--liquibase formatted sql
--changeset moirai:1774400010_alter_table_adventure_rename_creator_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure RENAME COLUMN creator_id TO created_by;

/* liquibase rollback
ALTER TABLE adventure RENAME COLUMN created_by TO creator_id;
*/
