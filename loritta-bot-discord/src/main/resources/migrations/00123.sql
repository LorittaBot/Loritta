ALTER TABLE minessingleplayermatches ADD COLUMN result VARCHAR(64) NULL;
ALTER TABLE minessingleplayermatches ADD COLUMN playfield JSONB NULL;
ALTER TABLE minessingleplayermatches ADD COLUMN picked_playfield JSONB NULL;
ALTER TABLE minessingleplayermatches ADD COLUMN last_tile_x INTEGER NULL;
ALTER TABLE minessingleplayermatches ADD COLUMN last_tile_y INTEGER NULL;