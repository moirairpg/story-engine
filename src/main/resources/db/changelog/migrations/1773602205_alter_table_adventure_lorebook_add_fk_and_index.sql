--liquibase formatted sql
--changeset moirai:alter_table_adventure_lorebook_add_fk_and_index
--preconditions onFail:HALT, onError:HALT

ALTER TABLE adventure_lorebook
    ALTER adventure_id DROP NOT NULL;

ALTER TABLE adventure_lorebook
    ADD CONSTRAINT fk_adventure_lorebook_adventure
    FOREIGN KEY (adventure_id) REFERENCES adventure(id) ON DELETE CASCADE;

CREATE INDEX idx_adventure_lorebook_adventure_id ON adventure_lorebook(adventure_id);

/* liquibase rollback
DROP INDEX idx_adventure_lorebook_adventure_id;
ALTER TABLE adventure_lorebook DROP CONSTRAINT fk_adventure_lorebook_adventure;
*/
