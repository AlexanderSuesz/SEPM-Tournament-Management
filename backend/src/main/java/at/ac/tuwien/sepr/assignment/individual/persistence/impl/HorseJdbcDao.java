package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.BreedDao;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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

  private static String SQL_INSERT_WITH_BREED = "INSERT INTO "
      + TABLE_NAME
      + " (name, sex, date_of_birth, height, weight, breed_id) VALUES (?, ?, ?, ?, ?, ?)";

  private static String SQL_INSERT_WITHOUT_BREED = "INSERT INTO "
      + TABLE_NAME
      + " (name, sex, date_of_birth, height, weight) VALUES (?, ?, ?, ?, ?)";

  private static final String SQL_UPDATE_WITH_BREED = "UPDATE " + TABLE_NAME
      + " SET name = ?"
      + "  , sex = ?"
      + "  , date_of_birth = ?"
      + "  , height = ?"
      + "  , weight = ?"
      + "  , breed_id = ?"
      + " WHERE id = ?";

  private static final String SQL_UPDATE_WITHOUT_BREED = "UPDATE " + TABLE_NAME
      + " SET name = ?"
      + "  , sex = ?"
      + "  , date_of_birth = ?"
      + "  , height = ?"
      + "  , weight = ?"
      + " WHERE id = ?";

  private static final String SQL_DELETE_BY_ID = "DELETE FROM " + TABLE_NAME
      + " WHERE id = ?";

  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate jdbcNamed;
  private final BreedDao breedDao;


  public HorseJdbcDao(
      NamedParameterJdbcTemplate jdbcNamed,
      JdbcTemplate jdbcTemplate,
      BreedDao breedDao) {
    this.jdbcTemplate = jdbcTemplate;
    this.jdbcNamed = jdbcNamed;
    this.breedDao = breedDao; // Necessary for checking for ConflictException when using update method
  }

  @Override
  public Horse getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    List<Horse> horses;
    try {
      horses = jdbcTemplate.query(SQL_SELECT_BY_ID, this::mapRow, id);
    } catch (Exception e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Failed to retrieve horse", e);
    }
    if (horses.isEmpty()) {
      LOG.warn("No horse with ID %d found".formatted(id));
      throw new NotFoundException("Horse not found");
    }
    if (horses.size() > 1) {
      // This should never happen - more than one horse found with this id!!
      throw new FatalException("Too many horses with this ID found");
    }
    return horses.get(0);
  }

  @Override
  public Horse add(HorseDetailDto horse) {
    LOG.trace("add({})", horse);
    int addedCount;
    KeyHolder keyHolder = new GeneratedKeyHolder(); // Will contain the key (id) of the newly added horse.
    try {
      if (horse.breed() == null) {
        addedCount = jdbcTemplate.update(con -> {
          PreparedStatement ps = con.prepareStatement(SQL_INSERT_WITHOUT_BREED, Statement.RETURN_GENERATED_KEYS);
          ps.setString(1, horse.name());
          ps.setString(2, horse.sex().toString());
          ps.setObject(3, horse.dateOfBirth());
          ps.setFloat(4, horse.height());
          ps.setFloat(5, horse.weight());
          return ps;
        }, keyHolder);

      } else {
        addedCount = jdbcTemplate.update(con -> {
          PreparedStatement ps = con.prepareStatement(SQL_INSERT_WITH_BREED, Statement.RETURN_GENERATED_KEYS);
          ps.setString(1, horse.name());
          ps.setString(2, horse.sex().toString());
          ps.setObject(3, horse.dateOfBirth());
          ps.setFloat(4, horse.height());
          ps.setFloat(5, horse.weight());
          ps.setDouble(6, horse.breed().id());
          return ps;
        }, keyHolder);
      }
    } catch (Exception e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Failed to add horse", e);
    }
    LOG.debug("addCount is {}", addedCount);
    if (addedCount > 1) {
      // This should never happen - more than one horse was added!!
      throw new FatalException("More than one horse was added");
    } else if (addedCount <= 0) {
      // This should never happen - no horse was added!!
      throw new FatalException("No horse was added");
    } else {
      try {
        Number lastInsertedId = keyHolder.getKey();
        if (lastInsertedId != null) {
          return getById(lastInsertedId.longValue());
        } else {
          // This should never happen - the new horse should be retrievable from the database!!
          throw new FatalException("Failed to retrieve newly added horse");
        }
      } catch (Exception e) {
        // This should never happen - the new horse should be retrievable from the database!!
        throw new FatalException("Failed to retrieve newly added horse", e);
      }
    }
  }

  @Override
  public void deleteById(long id) throws NotFoundException {
    LOG.trace("deleteById({})", id);
    getById(id); // Will throw exception if horse with given ID does not exist or exists multiple times.
    int deleted;
    try {
      deleted = jdbcTemplate.update(SQL_DELETE_BY_ID, id);
    } catch (Exception e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Failed to delete horse", e);
    }
    if (deleted <= 0) {
      throw new NotFoundException("Could not find this horse, because it does not exist");
    }
    if (deleted > 1) {
      // This should never happen - deleted more than one horse!!
      throw new FatalException("Deleted more than one horse");
    }
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
    try {
      return jdbcNamed.query(query, params, this::mapRow);
    } catch (Exception e) {
      // This should never happen - the execution of the SQL query caused an exception!!
      throw new FatalException("Couldn't search for horses");
    }
  }


  @Override
  public Horse update(HorseDetailDto horse) throws NotFoundException, ConflictException {
    LOG.trace("update({})", horse);
    int updated;
    if (horse.breed() != null) {
      HashSet<Long> set = new HashSet<>();
      set.add(horse.breed().id());
      if (this.breedDao.findBreedsById(set).isEmpty()) {
        throw new ConflictException("Trying to update a horse with a not existing breed " + horse.breed().name(), Collections.singletonList("breeds"));
      }
      try {
        updated = jdbcTemplate.update(SQL_UPDATE_WITH_BREED,
                horse.name(),
                horse.sex().toString(),
                horse.dateOfBirth(),
                horse.height(),
                horse.weight(),
                horse.breed().id(),
                horse.id());
      } catch (Exception e) {
        // This should never happen - the execution of the SQL query caused an exception!!
        throw new FatalException("Couldn't update the horse " + horse.name());
      }
    } else {
      try {
        updated = jdbcTemplate.update(SQL_UPDATE_WITHOUT_BREED,
                horse.name(),
                horse.sex().toString(),
                horse.dateOfBirth(),
                horse.height(),
                horse.weight(),
                horse.id());
      } catch (Exception e) {
        // This should never happen - the execution of the SQL query caused an exception!!
        throw new FatalException("Couldn't update the horse " + horse.name());
      }
    }
    if (updated <= 0) {
      throw new NotFoundException("Couldn't update the horse " + horse.name() + ", because it does not exist");
    }
    try {
      return getById(horse.id());
    } catch (NotFoundException e) {
      // This should never happen - couldn't find updated horse!!
      throw new FatalException("Couldn't retrieve updated horse " + horse.name());
    }
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
