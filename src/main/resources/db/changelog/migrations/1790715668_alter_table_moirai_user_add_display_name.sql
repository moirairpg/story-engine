--liquibase formatted sql
--changeset moirai:1790715668_alter_table_moirai_user_add_display_name
--preconditions onFail:HALT, onError:HALT

ALTER TABLE moirai_user
        ADD COLUMN display_name VARCHAR(32);

UPDATE moirai_user
   SET display_name = username
 WHERE display_name IS NULL;

ALTER TABLE moirai_user
      ALTER COLUMN display_name SET NOT NULL;

/* liquibase rollback
ALTER TABLE moirai_user
       DROP COLUMN display_name;
*/
