package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.BreedSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Breed;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.persistence.BreedDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Provides access functionality to the application's persistent data store regarding breeds.
 * This implementation utilizes JDBC for database access.
 */
@Repository
public class BreedJdbcDao implements BreedDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String TABLE_NAME = "breed";
  private static final String SQL_ALL =
      "SELECT * FROM " + TABLE_NAME;
  private static final String SQL_FIND_BY_IDS =
      "SELECT * FROM " + TABLE_NAME
          + " WHERE id IN (:ids)";
  private static final String SQL_SEARCH =
      "SELECT * FROM " + TABLE_NAME
          + " WHERE UPPER(name) LIKE UPPER('%'||:name||'%')";
  private static final String SQL_LIMIT_CLAUSE = " LIMIT :limit";

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public BreedJdbcDao(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Collection<Breed> allBreeds() {
    LOG.trace("allBreeds()");
    try {
      return jdbcTemplate.query(SQL_ALL, this::mapRow);
    } catch (Exception e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Couldn't retrieve all breeds");
    }
  }

  @Override
  public Collection<Breed> findBreedsById(Set<Long> breedIds) {
    LOG.trace("findBreedsById({})", breedIds);
    try {
      return jdbcTemplate.query(SQL_FIND_BY_IDS, Map.of("ids", breedIds), this::mapRow);
    } catch (Exception e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Couldn't retrieve chosen breeds");
    }
  }

  @Override
  public Collection<Breed> search(BreedSearchDto searchParams) {
    String query = SQL_SEARCH;
    if (searchParams.limit() != null) {
      query += SQL_LIMIT_CLAUSE;
    }
    try {
      return jdbcTemplate.query(query, new BeanPropertySqlParameterSource(searchParams), this::mapRow);
    } catch (Exception e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Couldn't retrieve chosen breeds");
    }
  }

  /**
   * Maps a row from the {@link ResultSet} to a Breed object.
   *
   * @param result the {@link ResultSet} containing the row data
   * @param rownum the row number
   * @return a Horse object mapped from the {@link ResultSet} row
   * @throws SQLException if an SQL error occurs while mapping the row
   */
  private Breed mapRow(ResultSet result, int rownum) throws SQLException {
    return new Breed()
        .setId(result.getLong("id"))
        .setName(result.getString("name"))
        ;
  }
}
