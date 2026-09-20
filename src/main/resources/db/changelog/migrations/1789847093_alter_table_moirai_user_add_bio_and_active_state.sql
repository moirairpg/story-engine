--liquibase formatted sql
--changeset moirai:1789847093_alter_table_moirai_user_add_bio_and_active_state
--preconditions onFail:HALT, onError:HALT

ALTER TABLE moirai_user
        ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE moirai_user
        ADD COLUMN bio VARCHAR(2000);

/* liquibase rollback
ALTER TABLE moirai_user
       DROP COLUMN bio;

ALTER TABLE moirai_user
       DROP COLUMN is_active;
*/
