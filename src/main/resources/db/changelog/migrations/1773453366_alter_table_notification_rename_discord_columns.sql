--liquibase formatted sql
--changeset moirai:1773453366_alter_table_notification_rename_discord_columns
--preconditions onFail:HALT onError:HALT

ALTER TABLE notification RENAME COLUMN creator_discord_id TO creator_id;

ALTER TABLE notification_read RENAME COLUMN creator_discord_id TO creator_id;

--rollback ALTER TABLE notification RENAME COLUMN creator_id TO creator_discord_id;
--rollback ALTER TABLE notification_read RENAME COLUMN creator_id TO creator_discord_id;
