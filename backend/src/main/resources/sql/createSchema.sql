CREATE TABLE IF NOT EXISTS breed
(
  id BIGINT PRIMARY KEY,
  name VARCHAR(32) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS horse
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  -- Instead of an ENUM (H2 specific) this could also be done with a character string type and a check constraint.
  sex ENUM ('MALE', 'FEMALE') NOT NULL,
  date_of_birth DATE NOT NULL,
  height NUMERIC(4,2),
  weight NUMERIC(7,2),
  // TODO handle optional everywhere
  breed_id BIGINT REFERENCES breed(id)
);

CREATE TABLE IF NOT EXISTS tournament
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL
);

-- a table which maps horses to tournaments, also saves their entry number and round reached inside the tournament
CREATE TABLE IF NOT EXISTS horse_mapped_to_tournament
(
    tournament_id BIGINT REFERENCES tournament (id),
    horse_id BIGINT REFERENCES horse (id),
    PRIMARY KEY (tournament_id, horse_id),
    entry_number BIGINT,
    round_reached BIGINT
);