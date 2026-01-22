ALTER TABLE partnerapplications ADD COLUMN submitter_permission_level VARCHAR(64);
UPDATE partnerapplications SET submitter_permission_level = 'ADMINISTRATOR' WHERE submitter_permission_level IS NULL;
ALTER TABLE partnerapplications ALTER COLUMN submitter_permission_level SET NOT NULL;
