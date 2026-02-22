ALTER TABLE raffletickets ADD COLUMN bought_tickets BIGINT;
UPDATE raffletickets SET bought_tickets = 1;
ALTER TABLE raffletickets ALTER COLUMN bought_tickets SET NOT NULL;
