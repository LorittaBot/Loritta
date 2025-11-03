ALTER TABLE userwebsitesessions ADD COLUMN cookie_set_at TIMESTAMPTZ, ADD COLUMN cookie_max_age INT;
UPDATE userwebsitesessions SET cookie_set_at = NOW(), cookie_max_age = 0;
ALTER TABLE userwebsitesessions ALTER COLUMN cookie_set_at SET NOT NULL, ALTER COLUMN cookie_max_age SET NOT NULL;
