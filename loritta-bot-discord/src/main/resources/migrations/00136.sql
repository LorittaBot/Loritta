ALTER TABLE eventlogconfigs ADD COLUMN messages_cleared BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE eventlogconfigs ADD COLUMN messages_cleared_log_channel BIGINT;
