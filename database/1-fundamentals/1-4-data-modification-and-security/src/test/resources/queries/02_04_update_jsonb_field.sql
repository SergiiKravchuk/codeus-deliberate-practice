-- check the schema for challenges table in 'resources/schema.sql'
-- update only the one field in the challenge task jsonb column
-- set days amount to 10
UPDATE challenges
SET challenge_task = jsonb_set(challenge_task, '{days}', '10')
WHERE challenge_name = 'algo-challenge';