-- Add the new stack entries system
ALTER TABLE giveaways ADD COLUMN extra_entries_should_stack BOOLEAN;

UPDATE giveaways SET extra_entries_should_stack = false WHERE extra_entries_should_stack IS NULL;

ALTER TABLE giveaways ALTER COLUMN extra_entries_should_stack SET NOT NULL;