--liquibase formatted sql
--changeset moirai:1774400007_update_table_moirai_user_migrate_creator_id_to_username
--preconditions onFail:HALT, onError:HALT

UPDATE moirai_user w SET creator_id = u.username
FROM moirai_user u WHERE u.discord_id = w.creator_id;

/* liquibase rollback
UPDATE moirai_user w SET creator_id = COALESCE(u.discord_id, w.creator_id)
FROM moirai_user u WHERE u.username = w.creator_id;
*/
