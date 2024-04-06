package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailParticipantDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Standing;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseMappedToTournamentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

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

  private static final String SQL_SELECT_BY_TOURNAMENTID = "SELECT * FROM "
      + TABLE_NAME
      + " WHERE tournament_id = ?";

  private static final String SQL_SELECT_SINGLE_MAPPING = "SELECT * FROM "
      + TABLE_NAME
      + " WHERE horse_id = ? AND tournament_id = ?";

  private static final String SQL_CHECK_IF_ENTRY_ALREADY_EXISTS = "SELECT COUNT(*) FROM " // should return 1 if the entry already exists and 0 if it doesn't
      + TABLE_NAME
      + " WHERE horse_id = ? AND tournament_id = ?";

  private static final String SQL_INSERT_WITHOUT_STANDING = "INSERT INTO "
      + TABLE_NAME
      + "  (tournament_id, horse_id) VALUES (?, ?)";

  private static final String SQL_UPDATE_ENTRY_AND_ROUND_NUMBER = "UPDATE " + TABLE_NAME
      + " SET entry_number = ?,"
      + " round_reached = ?"
      + " WHERE horse_id = ? AND tournament_id = ?";

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
  public List<Standing> getHorsesInTournament(long tournamentId) throws NotFoundException {
    LOG.trace("getHorsesInTournament({})", tournamentId);
    List<Standing> standings;
    try {
      standings = jdbcTemplate.query(SQL_SELECT_BY_TOURNAMENTID, this::mapRow, tournamentId);
    } catch (Exception e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Failed to retrieve standings for the horses in this tournament", e);
    }
    if (standings.isEmpty()) {
      throw new NotFoundException("No horses found taking part in this tournament");
    }
    return standings;
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

  @Override
  public Standing update(TournamentDetailParticipantDto horse, long tournamentId) throws NotFoundException {
    LOG.trace("update({}, {})", horse, tournamentId);
    int updated;
    try {
      updated = jdbcTemplate.update(SQL_UPDATE_ENTRY_AND_ROUND_NUMBER,
          horse.entryNumber(),
          horse.roundReached(),
          horse.horseId(),
          tournamentId);
    } catch (DataAccessException e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Couldn't update the tournament standings", e);
    }
    if (updated <= 0) {
      LOG.debug("There was no horse to tournament mapping found for the horse " + horse + " and the tournament " + tournamentId);
      throw new NotFoundException("Couldn't update the tournament standing, because it does not exist");
    }
    try {
      return getSingleMapping(horse.horseId(), tournamentId);
    } catch (NotFoundException e) {
      throw new FatalException("The standing for the horse " + horse.name() + " was updated for the tournament but it wasn't found in the database", e);
    }
  }

  @Override
  public Standing getSingleMapping(long horseId, long tournamentId) throws NotFoundException {
    LOG.trace("getSingleMapping({}, {})", horseId, tournamentId);
    List<Standing> standing;
    try {
      standing = jdbcTemplate.query(SQL_SELECT_SINGLE_MAPPING, this::mapRow, horseId, tournamentId);
    } catch (DataAccessException e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Failed to retrieve standing for a horse in this tournament", e);
    }
    if (standing.isEmpty()) {
      throw new NotFoundException("This horse doesn't take part in in this tournament");
    }
    if (standing.size() > 1) {
      throw new FatalException("More than one standing for the same horse found");
    }
    return standing.getFirst();
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
        .setEntryNumber((Long) result.getObject("entry_number"))
        .setRoundReached((Long) result.getObject("round_reached"))
        ;
  }
}
