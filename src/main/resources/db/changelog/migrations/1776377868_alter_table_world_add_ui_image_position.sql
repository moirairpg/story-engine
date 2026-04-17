--liquibase formatted sql
--changeset moirai:1776377868_alter_table_world_add_ui_image_position
--preconditions onFail:HALT, onError:HALT

ALTER TABLE world ADD COLUMN ui_image_position_x DECIMAL;
ALTER TABLE world ADD COLUMN ui_image_position_y DECIMAL;

/* liquibase rollback
ALTER TABLE world DROP COLUMN ui_image_position_x;
ALTER TABLE world DROP COLUMN ui_image_position_y;
*/
