--liquibase formatted sql
--changeset moirai:1773453365_alter_table_moirai_user_rename_discord_columns
--preconditions onFail:HALT onError:HALT

ALTER TABLE moirai_user RENAME COLUMN creator_discord_id TO creator_id;

--rollback ALTER TABLE moirai_user RENAME COLUMN creator_id TO creator_discord_id;
