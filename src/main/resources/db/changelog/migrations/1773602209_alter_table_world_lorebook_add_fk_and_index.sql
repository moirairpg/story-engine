--liquibase formatted sql
--changeset moirai:alter_table_world_lorebook_add_fk_and_index
--preconditions onFail:HALT, onError:HALT

ALTER TABLE world_lorebook
    ADD CONSTRAINT fk_world_lorebook_world
    FOREIGN KEY (world_id) REFERENCES world(id) ON DELETE CASCADE;

CREATE INDEX idx_world_lorebook_world_id ON world_lorebook(world_id);

/* liquibase rollback
DROP INDEX idx_world_lorebook_world_id;
ALTER TABLE world_lorebook DROP CONSTRAINT fk_world_lorebook_world;
*/
