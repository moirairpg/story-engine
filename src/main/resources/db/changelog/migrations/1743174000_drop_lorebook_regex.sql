--liquibase formatted sql

--changeset moirai:1743174000
ALTER TABLE world_lorebook DROP COLUMN regex;
ALTER TABLE adventure_lorebook DROP COLUMN regex;
