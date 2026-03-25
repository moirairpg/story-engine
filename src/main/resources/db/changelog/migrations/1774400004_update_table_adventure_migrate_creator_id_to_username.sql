--liquibase formatted sql
--changeset moirai:1774400004_update_table_adventure_migrate_creator_id_to_username
--preconditions onFail:HALT, onError:HALT

UPDATE adventure w SET creator_id = u.username
FROM moirai_user u WHERE u.discord_id = w.creator_id;

/* liquibase rollback
UPDATE adventure w SET creator_id = COALESCE(u.discord_id, w.creator_id)
FROM moirai_user u WHERE u.username = w.creator_id;
*/
