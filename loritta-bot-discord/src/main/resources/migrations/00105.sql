ALTER TABLE userpocketlorittasettings ADD COLUMN activity_level VARCHAR(64);

UPDATE userpocketlorittasettings SET activity_level = 'MEDIUM';

ALTER TABLE userpocketlorittasettings ALTER COLUMN activity_level SET NOT NULL;
