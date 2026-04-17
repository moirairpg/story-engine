--liquibase formatted sql
--changeset moirai:1774400012_alter_table_adventure_lorebook_rename_creator_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure_lorebook RENAME COLUMN creator_id TO created_by;

/* liquibase rollback
ALTER TABLE adventure_lorebook RENAME COLUMN created_by TO creator_id;
*/
