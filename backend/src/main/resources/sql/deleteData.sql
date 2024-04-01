-- a foreign key constraint is used to prevent the destruction of links between tables.
-- therefore, we need to drop the foreign key constraint.
-- drop foreign key constraint in horse_mapped_to_tournament for tournament_id
ALTER TABLE horse_mapped_to_tournament DROP CONSTRAINT IF EXISTS tournament_id;
-- drop foreign key constraint in horse_mapped_to_tournament for horse_id
ALTER TABLE horse_mapped_to_tournament DROP CONSTRAINT IF EXISTS horse_id;
-- drop foreign key constraint in horse
ALTER TABLE horse DROP CONSTRAINT IF EXISTS breed_id;

DELETE FROM horse_mapped_to_tournament;
DELETE FROM horse;
DELETE FROM breed;
DELETE FROM tournament;