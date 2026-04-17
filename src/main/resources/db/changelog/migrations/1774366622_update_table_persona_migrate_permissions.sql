--liquibase formatted sql
--changeset moirai:1774366622_update_table_persona_migrate_permissions
--preconditions onFail:HALT, onError:HALT

INSERT INTO persona_permissions (persona_id, user_id, permission)
SELECT p.id, u.id, 'OWNER'
FROM persona p
JOIN moirai_user u ON u.discord_id = p.owner_id
WHERE p.owner_id IS NOT NULL;

INSERT INTO persona_permissions (persona_id, user_id, permission)
SELECT p.id, u.id, 'WRITE'
FROM persona p
JOIN moirai_user u ON u.discord_id = ANY(string_to_array(p.users_allowed_to_write, ','))
WHERE p.users_allowed_to_write IS NOT NULL AND p.users_allowed_to_write <> '';

INSERT INTO persona_permissions (persona_id, user_id, permission)
SELECT p.id, u.id, 'READ'
FROM persona p
JOIN moirai_user u ON u.discord_id = ANY(string_to_array(p.users_allowed_to_read, ','))
WHERE p.users_allowed_to_read IS NOT NULL AND p.users_allowed_to_read <> '';

/* liquibase rollback
DELETE FROM persona_permissions;
*/
