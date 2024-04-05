package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Tournament;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.TournamentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

/**
 * Provides access functionality to the application's persistent data store regarding tournaments.
 * This implementation utilizes JDBC for database access.
 */
@Repository
public class TournamentJdbcDao implements TournamentDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String TABLE_NAME = "tournament";
  private static final String SQL_SELECT_SEARCH = "SELECT  "
      + "    t.id as \"id\", t.name as \"name\", t.start_date as \"start_date\""
      + "    , t.end_date as \"end_date\""
      + " FROM " + TABLE_NAME + " t"
      + " WHERE (:name IS NULL OR UPPER(t.name) LIKE UPPER('%'||:name||'%'))"
      + " AND ((:earliestTournamentDay IS NULL AND :latestTournamentDay IS NULL) OR"
      + "      (:latestTournamentDay IS NULL AND t.end_date >= :earliestTournamentDay) OR"
      + "      (:earliestTournamentDay IS NULL AND t.start_date <= :latestTournamentDay) OR"
      + "      (t.end_date >= :earliestTournamentDay AND t.start_date <= :latestTournamentDay)"
      + "     )"
      + " ORDER BY t.start_date DESC"; // orders the resulting tournaments by their descending start date

  private static final String SQL_SELECT_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";

  private static final String SQL_INSERT_TOURNAMENT = "INSERT INTO "
      + TABLE_NAME
      + " (name, start_date, end_date) VALUES (?, ?, ?)";
  private static final String SQL_LIMIT_CLAUSE = " LIMIT :limit";
  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate jdbcNamed;


  public TournamentJdbcDao(
      NamedParameterJdbcTemplate jdbcNamed,
      JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.jdbcNamed = jdbcNamed;
  }

  @Override
  public Tournament getTournamentDetailsById(long id) throws NotFoundException {
    LOG.trace("getTournamentDetailsById({})", id);
    List<Tournament> tournaments;
    try {
      tournaments = jdbcTemplate.query(SQL_SELECT_BY_ID, this::mapRow, id);
    } catch (DataAccessException e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Failed to retrieve tournament", e);
    }
    if (tournaments.isEmpty()) {
      throw new NotFoundException("Tournament not found");
    }
    if (tournaments.size() > 1) {
      // This should never happen - more than one tournament found with this id!!
      throw new FatalException("Too many tournaments with this ID found");
    }
    return tournaments.getFirst();
  }

  @Override
  public Collection<Tournament> search(TournamentSearchDto searchParameters) {
    LOG.trace("search({})", searchParameters);
    var query = SQL_SELECT_SEARCH;
    if (searchParameters.limit() != null) {
      query += SQL_LIMIT_CLAUSE;
    }
    var params = new BeanPropertySqlParameterSource(searchParameters);
    try {
      return jdbcNamed.query(query, params, this::mapRow);
    } catch (DataAccessException e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Couldn't search for tournaments", e);
    }
  }

  @Override
  public Tournament add(TournamentCreateDto tournament) {
    LOG.trace("add({})", tournament);
    int addedCount;
    KeyHolder keyHolder = new GeneratedKeyHolder(); // Will contain the key (id) of the newly added tournament.
    try {
      addedCount = jdbcTemplate.update(con -> {
        PreparedStatement ps = con.prepareStatement(SQL_INSERT_TOURNAMENT, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, tournament.name());
        ps.setObject(2, tournament.startDate());
        ps.setObject(3, tournament.endDate());
        return ps;
      }, keyHolder);
    } catch (DataAccessException e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Failed to add tournament", e);
    }
    LOG.debug("The tournament ({}) was added {} times to the database", tournament, addedCount);
    if (addedCount > 1) {
      // This should never happen - more than one tournament was added!!
      throw new FatalException("More than one tournament was added");
    } else if (addedCount <= 0) {
      // This should never happen - no tournament was added!!
      throw new FatalException("No tournament was added");
    }
    Number lastInsertedId = keyHolder.getKey();
    if (lastInsertedId == null) {
      // This should never happen - the new tournament should be retrievable from the database!!
      throw new FatalException("Failed to identify newly added tournament");
    }
    LOG.debug("A new tournament with the id {} was created", lastInsertedId);
    return new Tournament()
        .setId((long) lastInsertedId)
        .setName(tournament.name())
        .setStartDate(tournament.startDate())
        .setEndDate(tournament.endDate());
  }

  /**
   * Maps a row from the {@link ResultSet} to a Tournaments object.
   *
   * @param result the {@link ResultSet} containing the row data
   * @param rownum the row number
   * @return a Tournament object mapped from the {@link ResultSet} row
   * @throws SQLException if an SQL error occurs while mapping the row
   */
  private Tournament mapRow(ResultSet result, int rownum) throws SQLException {
    return new Tournament()
        .setId(result.getLong("id"))
        .setName(result.getString("name"))
        .setStartDate(result.getDate("start_date").toLocalDate())
        .setEndDate(result.getDate("end_date").toLocalDate())
        ;
  }
}
