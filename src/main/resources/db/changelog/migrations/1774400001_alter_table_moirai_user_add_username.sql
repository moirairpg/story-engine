--liquibase formatted sql
--changeset moirai:1774400001_alter_table_moirai_user_add_username
--preconditions onFail:HALT, onError:HALT

ALTER TABLE moirai_user ADD COLUMN username VARCHAR(100);
UPDATE moirai_user SET username = discord_id WHERE username IS NULL;
ALTER TABLE moirai_user ALTER COLUMN username SET NOT NULL;

/* liquibase rollback
ALTER TABLE moirai_user DROP COLUMN username;
*/
