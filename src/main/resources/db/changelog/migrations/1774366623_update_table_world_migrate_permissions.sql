--liquibase formatted sql
--changeset moirai:1774366623_update_table_world_migrate_permissions
--preconditions onFail:HALT, onError:HALT

INSERT INTO world_permissions (world_id, user_id, permission)
SELECT w.id, u.id, 'OWNER'
FROM world w
JOIN moirai_user u ON u.discord_id = w.owner_id
WHERE w.owner_id IS NOT NULL;

INSERT INTO world_permissions (world_id, user_id, permission)
SELECT w.id, u.id, 'WRITE'
FROM world w
JOIN moirai_user u ON u.discord_id = ANY(string_to_array(w.users_allowed_to_write, ','))
WHERE w.users_allowed_to_write IS NOT NULL AND w.users_allowed_to_write <> '';

INSERT INTO world_permissions (world_id, user_id, permission)
SELECT w.id, u.id, 'READ'
FROM world w
JOIN moirai_user u ON u.discord_id = ANY(string_to_array(w.users_allowed_to_read, ','))
WHERE w.users_allowed_to_read IS NOT NULL AND w.users_allowed_to_read <> '';

/* liquibase rollback
DELETE FROM world_permissions;
*/
