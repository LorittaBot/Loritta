ALTER TABLE MarriageLoveLetters ADD COLUMN affinity_reward BOOLEAN;

UPDATE MarriageLoveLetters SET affinity_reward = true WHERE affinity_reward IS NULL;

ALTER TABLE MarriageLoveLetters ALTER COLUMN affinity_reward SET NOT NULL;