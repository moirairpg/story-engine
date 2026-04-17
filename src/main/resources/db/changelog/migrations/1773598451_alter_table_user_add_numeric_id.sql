--liquibase formatted sql
--changeset moirai:1773598451_alter_table_user_add_numeric_id
--preconditions onFail:HALT, onError:HALT

ALTER TABLE moirai_user ADD COLUMN numeric_id BIGINT GENERATED ALWAYS AS IDENTITY;

UPDATE user_backup
   SET numeric_id = t.numeric_id
  FROM moirai_user t
 WHERE user_backup.nano_id = t.id;

ALTER TABLE moirai_user DROP COLUMN id;
ALTER TABLE moirai_user RENAME COLUMN numeric_id TO id;
ALTER TABLE moirai_user ADD PRIMARY KEY (id);

/* liquibase rollback
ALTER TABLE moirai_user ADD COLUMN nano_id VARCHAR(100);

UPDATE moirai_user
   SET nano_id = b.nano_id
  FROM user_backup b
 WHERE moirai_user.public_id = b.uuid;

ALTER TABLE moirai_user DROP COLUMN id;
ALTER TABLE moirai_user RENAME COLUMN nano_id TO id;
ALTER TABLE moirai_user ADD PRIMARY KEY (id);
*/
