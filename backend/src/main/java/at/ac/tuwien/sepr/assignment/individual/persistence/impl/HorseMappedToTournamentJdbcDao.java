package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.entity.Standing;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseMappedToTournamentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

/**
 * Provides access functionality to the application's persistent data store regarding the mapping of tournaments to horses.
 * This implementation utilizes JDBC for database access.
 */
@Repository
public class HorseMappedToTournamentJdbcDao implements HorseMappedToTournamentDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String TABLE_NAME = "horse_mapped_to_tournament";
  private static final String SQL_COUNT_TOURNAMENTS_FOR_HORSE = "SELECT COUNT(*) FROM "
      + TABLE_NAME
      + " WHERE horse_id = ?";

  private static final String SQL_CHECK_IF_ENTRY_ALREADY_EXISTS = "SELECT COUNT(*) FROM " // should return 1 if the entry already exists and 0 if it doesn't
      + TABLE_NAME
      + " WHERE horse_id = ? AND tournament_id = ?";

  private static final String SQL_INSERT_WITHOUT_STANDING = "INSERT INTO "
      + TABLE_NAME
      + "  (tournament_id, horse_id) VALUES (?, ?)";

  private final JdbcTemplate jdbcTemplate;

  public HorseMappedToTournamentJdbcDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public long countTournamentsForHorse(long horseId) {
    LOG.trace("countTournamentsForHorse({})", horseId);
    try {
      return jdbcTemplate.queryForObject(SQL_COUNT_TOURNAMENTS_FOR_HORSE, Long.class, horseId);
    } catch (Exception e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Failed to count tournaments for horse", e);
    }
  }

  public int countOccurrenceOfEntry(long horseId, long tournamentId) {
    LOG.trace("countOccurrenceOfEntry({}, {})", horseId, tournamentId);
    try {
      return jdbcTemplate.queryForObject(SQL_CHECK_IF_ENTRY_ALREADY_EXISTS, Integer.class, horseId, tournamentId);
    } catch (Exception e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Failed to count occurrence of new horse to tournament mapping in database", e);
    }
  }

  @Override
  public Standing add(long horseId, long tournamentId) throws ConflictException {
    LOG.trace("add({}, {})", horseId, tournamentId);
    int elementOccurrenceCount; // Will be 0 if element isn't present in the database
    try {
      elementOccurrenceCount = countOccurrenceOfEntry(horseId, tournamentId);
    } catch (Exception e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Failed to count occurrence of new horse to tournament mapping in database", e);
    }
    LOG.debug("Adding the new entry ({}, {}) to the standings. Their current occurrence count in the database: {}",
        horseId, tournamentId, elementOccurrenceCount);
    if (elementOccurrenceCount > 0) {
      throw new ConflictException("There already exists a tournament standing for this horse inside this tournament",
          Collections.singletonList("mapping already found in database"));
    }
    int addedCount;
    try {
      addedCount = jdbcTemplate.update(SQL_INSERT_WITHOUT_STANDING, tournamentId, horseId);
    } catch (Exception e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Failed to add mapping of new horse to tournament to the database", e);
    }
    LOG.debug("The horse tournament mapping ({}, {}) was added {} times", horseId, tournamentId, addedCount);
    if (addedCount > 1) {
      // This should never happen - more than one horse tournament mapping was added!!
      throw new FatalException("More than one horse tournament mapping was added");
    } else if (addedCount <= 0) {
      // This should never happen - no horse tournament mapping was added!!
      throw new FatalException("No horse tournament mapping was added");
    }
    return new Standing()
        .setHorseId(horseId)
        .setTournamentId(tournamentId)
        .setRoundReached(null)
        .setEntryNumber(null);
  }

  /**
   * Maps a row from the {@link ResultSet} to a horse_mapped_to_tournament object.
   *
   * @param result the {@link ResultSet} containing the row data
   * @param rownum the row number
   * @return a Standing object mapped from the {@link ResultSet} row
   * @throws SQLException if an SQL error occurs while mapping the row
   */
  private Standing mapRow(ResultSet result, int rownum) throws SQLException {
    return new Standing()
        .setHorseId(result.getLong("horse_id"))
        .setTournamentId(result.getLong("tournament_id"))
        .setEntryNumber(result.getLong("entry_number"))
        .setRoundReached(result.getLong("round_reached"))
        ;
  }
}