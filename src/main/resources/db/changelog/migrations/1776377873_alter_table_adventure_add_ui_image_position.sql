--liquibase formatted sql
--changeset moirai:1776377873_alter_table_adventure_add_ui_image_position
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure ADD COLUMN ui_image_position_x DECIMAL;
ALTER TABLE adventure ADD COLUMN ui_image_position_y DECIMAL;

/* liquibase rollback
ALTER TABLE adventure DROP COLUMN ui_image_position_x;
ALTER TABLE adventure DROP COLUMN ui_image_position_y;
*/
