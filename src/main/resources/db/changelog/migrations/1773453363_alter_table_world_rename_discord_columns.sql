--liquibase formatted sql
--changeset moirai:1773453363_alter_table_world_rename_discord_columns
--preconditions onFail:HALT onError:HALT

ALTER TABLE world RENAME COLUMN owner_discord_id TO owner_id;
ALTER TABLE world RENAME COLUMN creator_discord_id TO creator_id;
ALTER TABLE world RENAME COLUMN discord_users_allowed_to_read TO users_allowed_to_read;
ALTER TABLE world RENAME COLUMN discord_users_allowed_to_write TO users_allowed_to_write;

ALTER TABLE world_lorebook RENAME COLUMN creator_discord_id TO creator_id;

--rollback ALTER TABLE world RENAME COLUMN owner_id TO owner_discord_id;
--rollback ALTER TABLE world RENAME COLUMN creator_id TO creator_discord_id;
--rollback ALTER TABLE world RENAME COLUMN users_allowed_to_read TO discord_users_allowed_to_read;
--rollback ALTER TABLE world RENAME COLUMN users_allowed_to_write TO discord_users_allowed_to_write;
--rollback ALTER TABLE world_lorebook RENAME COLUMN creator_id TO creator_discord_id;
