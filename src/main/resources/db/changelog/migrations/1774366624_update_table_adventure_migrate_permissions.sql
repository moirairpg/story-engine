--liquibase formatted sql
--changeset moirai:1774366624_update_table_adventure_migrate_permissions
--preconditions onFail:HALT, onError:HALT

INSERT INTO adventure_permissions (adventure_id, user_id, permission)
SELECT a.id, u.id, 'OWNER'
FROM adventure a
JOIN moirai_user u ON u.discord_id = a.owner_id
WHERE a.owner_id IS NOT NULL;

INSERT INTO adventure_permissions (adventure_id, user_id, permission)
SELECT a.id, u.id, 'WRITE'
FROM adventure a
JOIN moirai_user u ON u.discord_id = ANY(string_to_array(a.users_allowed_to_write, ','))
WHERE a.users_allowed_to_write IS NOT NULL AND a.users_allowed_to_write <> '';

INSERT INTO adventure_permissions (adventure_id, user_id, permission)
SELECT a.id, u.id, 'READ'
FROM adventure a
JOIN moirai_user u ON u.discord_id = ANY(string_to_array(a.users_allowed_to_read, ','))
WHERE a.users_allowed_to_read IS NOT NULL AND a.users_allowed_to_read <> '';

/* liquibase rollback
DELETE FROM adventure_permissions;
*/
