--liquibase formatted sql
--changeset moirai:1773453362_alter_table_adventure_rename_discord_columns
--preconditions onFail:HALT onError:HALT

ALTER TABLE adventure RENAME COLUMN discord_channel_id TO channel_id;
ALTER TABLE adventure RENAME COLUMN owner_discord_id TO owner_id;
ALTER TABLE adventure RENAME COLUMN creator_discord_id TO creator_id;
ALTER TABLE adventure RENAME COLUMN discord_users_allowed_to_read TO users_allowed_to_read;
ALTER TABLE adventure RENAME COLUMN discord_users_allowed_to_write TO users_allowed_to_write;

ALTER TABLE adventure_lorebook RENAME COLUMN player_discord_id TO player_id;
ALTER TABLE adventure_lorebook RENAME COLUMN creator_discord_id TO creator_id;

--rollback ALTER TABLE adventure RENAME COLUMN channel_id TO discord_channel_id;
--rollback ALTER TABLE adventure RENAME COLUMN owner_id TO owner_discord_id;
--rollback ALTER TABLE adventure RENAME COLUMN creator_id TO creator_discord_id;
--rollback ALTER TABLE adventure RENAME COLUMN users_allowed_to_read TO discord_users_allowed_to_read;
--rollback ALTER TABLE adventure RENAME COLUMN users_allowed_to_write TO discord_users_allowed_to_write;
--rollback ALTER TABLE adventure_lorebook RENAME COLUMN player_id TO player_discord_id;
--rollback ALTER TABLE adventure_lorebook RENAME COLUMN creator_id TO creator_discord_id;
