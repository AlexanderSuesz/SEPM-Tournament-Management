package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Tournament;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.persistence.TournamentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

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
  public Collection<Tournament> search(TournamentSearchDto searchParameters) {
    LOG.trace("search({})", searchParameters);
    var query = SQL_SELECT_SEARCH;
    if (searchParameters.limit() != null) {
      query += SQL_LIMIT_CLAUSE;
    }
    var params = new BeanPropertySqlParameterSource(searchParameters);
    try {
      return jdbcNamed.query(query, params, this::mapRow);
    } catch (Exception e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Couldn't search for tournaments");
    }
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
