--liquibase formatted sql
--changeset moirai:1773453364_alter_table_persona_rename_discord_columns
--preconditions onFail:HALT onError:HALT

ALTER TABLE persona RENAME COLUMN owner_discord_id TO owner_id;
ALTER TABLE persona RENAME COLUMN creator_discord_id TO creator_id;
ALTER TABLE persona RENAME COLUMN discord_users_allowed_to_read TO users_allowed_to_read;
ALTER TABLE persona RENAME COLUMN discord_users_allowed_to_write TO users_allowed_to_write;

--rollback ALTER TABLE persona RENAME COLUMN owner_id TO owner_discord_id;
--rollback ALTER TABLE persona RENAME COLUMN creator_id TO creator_discord_id;
--rollback ALTER TABLE persona RENAME COLUMN users_allowed_to_read TO discord_users_allowed_to_read;
--rollback ALTER TABLE persona RENAME COLUMN users_allowed_to_write TO discord_users_allowed_to_write;
