package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;

/**
 * Provides access functionality to the application's persistent data store regarding horses.
 * This implementation utilizes JDBC for database access.
 */
@Repository
public class HorseJdbcDao implements HorseDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TABLE_NAME = "horse";
  private static final String SQL_SELECT_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
  private static final String SQL_SELECT_SEARCH = "SELECT  "
      + "    h.id as \"id\", h.name as \"name\", h.sex as \"sex\", h.date_of_birth as \"date_of_birth\""
      + "    , h.height as \"height\", h.weight as \"weight\", h.breed_id as \"breed_id\""
      + " FROM " + TABLE_NAME + " h LEFT OUTER JOIN breed b ON (h.breed_id = b.id)"
      + " WHERE (:name IS NULL OR UPPER(h.name) LIKE UPPER('%'||:name||'%'))"
      + "  AND (:sex IS NULL OR :sex = sex)"
      + "  AND (:bornEarliest IS NULL OR :bornEarliest <= h.date_of_birth)"
      + "  AND (:bornLatest IS NULL OR :bornLatest >= h.date_of_birth)"
      + "  AND (:breed IS NULL OR UPPER(b.name) LIKE UPPER('%'||:breed||'%'))";

  private static final String SQL_LIMIT_CLAUSE = " LIMIT :limit";

  private static String SQL_INSERT = "INSERT INTO "
      + TABLE_NAME
      + " (name, sex, date_of_birth, height, weight, breed_id) VALUES (?, ?, ?, ?, ?, ?)";

  private static String SQL_INSERT_WITHOUT_BREED = "INSERT INTO "
      + TABLE_NAME
      + " (name, sex, date_of_birth, height, weight) VALUES (?, ?, ?, ?, ?)";

  private static final String SQL_UPDATE = "UPDATE " + TABLE_NAME
      + " SET name = ?"
      + "  , sex = ?"
      + "  , date_of_birth = ?"
      + "  , height = ?"
      + "  , weight = ?"
      + "  , breed_id = ?"
      + " WHERE id = ?";

  private static final String SQL_DELETE_BY_ID = "DELETE FROM " + TABLE_NAME
      + " WHERE id = ?";

  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate jdbcNamed;


  public HorseJdbcDao(
      NamedParameterJdbcTemplate jdbcNamed,
      JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.jdbcNamed = jdbcNamed;
  }

  @Override
  public Horse getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    List<Horse> horses;
    horses = jdbcTemplate.query(SQL_SELECT_BY_ID, this::mapRow, id);
    if (horses.isEmpty()) {
      throw new NotFoundException("No horse with ID %d found".formatted(id));
    }
    if (horses.size() > 1) {
      // This should never happen!!
      throw new FatalException("Too many horses with ID %d found".formatted(id));
    }

    return horses.get(0);
  }

  @Override
  public Horse add(HorseDetailDto horse) {
    LOG.trace("add({})", horse);
    int addedCount;
    Horse addedHorse;
    if (horse.breed() == null) {
      addedCount = jdbcTemplate.update(SQL_INSERT_WITHOUT_BREED,
          horse.name(),
          horse.sex().toString(),
          horse.dateOfBirth(),
          horse.height(),
          horse.weight());

      addedHorse = new Horse()
          .setName(horse.name())
          .setSex(horse.sex())
          .setDateOfBirth(horse.dateOfBirth())
          .setHeight(horse.height())
          .setWeight(horse.weight())
      ;
    } else {
      addedCount = jdbcTemplate.update(SQL_INSERT,
          horse.name(),
          horse.sex().toString(),
          horse.dateOfBirth(),
          horse.height(),
          horse.weight(),
          horse.breed().id());

      addedHorse = new Horse()
          .setName(horse.name())
          .setSex(horse.sex())
          .setDateOfBirth(horse.dateOfBirth())
          .setHeight(horse.height())
          .setWeight(horse.weight())
          .setBreedId(horse.breed().id())
      ;
    }
    if (addedCount > 1) {
      // This should never happen!!
      throw new FatalException("More than one horse was added");
    } else if (addedCount <= 0) {
      // This should never happen!!
      throw new FatalException("No horse was added");
    } else {
      return addedHorse;
    }
  }

  @Override
  public Horse deleteById(long id) throws NotFoundException {
    LOG.trace("deleteById({})", id);
    Horse deletedHorse = getById(id); // Will throw exception if horse with given ID does not exist or exists multiple times.
    int deleted = jdbcTemplate.update(SQL_DELETE_BY_ID, id);
    if (deleted == 0) {
      throw new NotFoundException("Could not update horse with ID " + id + ", because it does not exist");
    }
    if (deleted > 1) {
      throw new FatalException("Deleted more than one horse with the ID " + id);
    }
    return deletedHorse;
  }

  @Override
  public Collection<Horse> search(HorseSearchDto searchParameters) {
    LOG.trace("search({})", searchParameters);
    var query = SQL_SELECT_SEARCH;
    if (searchParameters.limit() != null) {
      query += SQL_LIMIT_CLAUSE;
    }
    var params = new BeanPropertySqlParameterSource(searchParameters);
    params.registerSqlType("sex", Types.VARCHAR);

    return jdbcNamed.query(query, params, this::mapRow);
  }


  @Override
  public Horse update(HorseDetailDto horse) throws NotFoundException {
    LOG.trace("update({})", horse);
    int updated = jdbcTemplate.update(SQL_UPDATE,
        horse.name(),
        horse.sex().toString(),
        horse.dateOfBirth(),
        horse.height(),
        horse.weight(),
        horse.breed().id(),
        horse.id());
    if (updated == 0) {
      throw new NotFoundException("Could not update horse with ID " + horse.id() + ", because it does not exist");
    }
    //TODO: check for ConflictException regarding breed not found in db.

    return new Horse()
        .setId(horse.id())
        .setName(horse.name())
        .setSex(horse.sex())
        .setDateOfBirth(horse.dateOfBirth())
        .setHeight(horse.height())
        .setWeight(horse.weight())
        .setBreedId(horse.breed().id())
        ;
  }

  /**
   * Maps a row from the {@link ResultSet} to a Horse object.
   *
   * @param result the {@link ResultSet} containing the row data
   * @param rownum the row number
   * @return a Horse object mapped from the {@link ResultSet} row
   * @throws SQLException if an SQL error occurs while mapping the row
   */
  private Horse mapRow(ResultSet result, int rownum) throws SQLException {
    return new Horse()
        .setId(result.getLong("id"))
        .setName(result.getString("name"))
        .setSex(Sex.valueOf(result.getString("sex")))
        .setDateOfBirth(result.getDate("date_of_birth").toLocalDate())
        .setHeight(result.getFloat("height"))
        .setWeight(result.getFloat("weight"))
        .setBreedId(result.getObject("breed_id", Long.class))
        ;
  }
}
