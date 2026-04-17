--liquibase formatted sql
--changeset moirai:1774400011_alter_table_world_lorebook_rename_creator_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE world_lorebook RENAME COLUMN creator_id TO created_by;

/* liquibase rollback
ALTER TABLE world_lorebook RENAME COLUMN created_by TO creator_id;
*/
