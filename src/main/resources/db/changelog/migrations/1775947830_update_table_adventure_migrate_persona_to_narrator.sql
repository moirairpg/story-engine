--liquibase formatted sql
--changeset moirai:1775947830_update_table_adventure_migrate_persona_to_narrator
--preconditions onFail:HALT, onError:HALT

UPDATE adventure a
SET narrator_name = p.name,
    narrator_personality = p.personality
FROM persona p
WHERE a.persona_id = p.id;

--rollback UPDATE adventure SET narrator_name = NULL, narrator_personality = NULL;
