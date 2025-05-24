ALTER TABLE UserMarriages ADD COLUMN affinity INTEGER;

UPDATE UserMarriages SET affinity = 20;

ALTER TABLE UserMarriages ALTER COLUMN affinity SET NOT NULL;

CREATE INDEX usermarriages_affinity ON UserMarriages USING btree (affinity);