--liquibase formatted sql
--changeset moirai:1776297288_alter_table_adventure_add_image_key
--preconditions onFail:HALT, onError:HALT
ALTER TABLE adventure ADD COLUMN image_key VARCHAR(500);
/* liquibase rollback ALTER TABLE adventure DROP COLUMN image_key; */
