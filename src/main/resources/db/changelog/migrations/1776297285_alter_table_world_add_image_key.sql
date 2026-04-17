--liquibase formatted sql
--changeset moirai:1776297285_alter_table_world_add_image_key
--preconditions onFail:HALT, onError:HALT
ALTER TABLE world ADD COLUMN image_key VARCHAR(500);
/* liquibase rollback ALTER TABLE world DROP COLUMN image_key; */
