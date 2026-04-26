--liquibase formatted sql
--changeset moirai:1777229540_alter_table_chronicle_segment_add_adventure_fk
--preconditions onFail:HALT, onError:HALT

ALTER TABLE chronicle_segment
    ADD CONSTRAINT fk_chronicle_segment_adventure
    FOREIGN KEY (adventure_id) REFERENCES adventure(id)
    ON DELETE CASCADE;

/* liquibase rollback
ALTER TABLE chronicle_segment
    DROP CONSTRAINT fk_chronicle_segment_adventure;
*/
