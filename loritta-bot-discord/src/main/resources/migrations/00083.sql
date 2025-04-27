-- Add the new weights
ALTER TABLE giveawayparticipants ADD COLUMN weight INTEGER;

UPDATE giveawayparticipants SET weight = 1 WHERE weight IS NULL;

ALTER TABLE giveawayparticipants ALTER COLUMN weight SET NOT NULL;