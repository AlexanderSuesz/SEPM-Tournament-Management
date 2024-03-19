package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator class for performing validation checks on horse data.
 * This class ensures that the data provided for a horse entity is valid.
 */
@Component
public class HorseValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Validates the provided horse details before performing an update operation.
   *
   * @param horse the horse details to validate
   * @throws ValidationException if validation fails due to invalid data
   */
  public void validateForUpdate(HorseDetailDto horse) throws ValidationException {
    LOG.trace("validateForUpdate({})", horse);
    List<String> validationErrors = new ArrayList<>();
    if (horse.id() == null) {
      validationErrors.add("No ID given");
    }
    validationErrors.addAll(validateBirthdateNotInFuture(horse.dateOfBirth()));
    if (horse.breed() != null) {
      validationErrors.addAll(validateBreed(horse.breed().name()));
    }
    validationErrors.addAll(validateName(horse.name()));
    validationErrors.addAll(validateHeight(horse.height()));
    validationErrors.addAll(validateWeight(horse.weight()));
    {
      if (!validationErrors.isEmpty()) {
        throw new ValidationException("Validation of horse for update failed", validationErrors);
      }
    }
  }

  /**
   * Validates the provided horse details before performing an insert operation (adding a new horse to the persistence storage).
   *
   * @param horse the horse details to validate
   * @throws ValidationException if validation fails due to invalid data
   */
  public void validateForInsert(HorseDetailDto horse) throws ValidationException {
    LOG.trace("validateForUpdate({})", horse);
    List<String> validationErrors = new ArrayList<>();
    validationErrors.addAll(validateBirthdateNotInFuture(horse.dateOfBirth()));
    if (horse.breed() != null) {
      validationErrors.addAll(validateBreed(horse.breed().name()));
    }
    validationErrors.addAll(validateName(horse.name()));
    validationErrors.addAll(validateHeight(horse.height()));
    validationErrors.addAll(validateWeight(horse.weight()));
    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }
  }

  /**
   * Validates the provided search parameters before performing the search operation (searching for the horses in the persistence storage).
   *
   * @param searchParameters the search parameters after which horses should be selected
   * @throws ValidationException if validation fails due to invalid data
   */
  public void validateForSearch(HorseSearchDto searchParameters) throws ValidationException {
    LOG.trace("validateForSearch({})", searchParameters);
    List<String> validationErrors = new ArrayList<>();
    if (searchParameters.limit() != null && searchParameters.limit() < 0) {
      validationErrors.add("There can be can't be less than 0 horses displayed");
    }
    if (searchParameters.bornEarliest() != null && searchParameters.bornLatest() != null
        && searchParameters.bornEarliest().isAfter(searchParameters.bornLatest())) {
      validationErrors.add("Earliest date of birth needs to be before latest date of birth");
    }
    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }
  }

  private List<String> validateBreed(String breed) {
    List<String> validationErrors = new ArrayList<>();
    if (breed.length() > 100) {
      validationErrors.add("Name of the horse breed is too long");
    }
    if (breed.length() <= 0) {
      validationErrors.add("Name of the horse breed is too short");
    }
    return validationErrors;
  }

  private List<String> validateName(String name) {
    List<String> validationErrors = new ArrayList<>();
    if (name.length() > 100) {
      validationErrors.add("Horse name is too long");
    }
    if (name.length() <= 0) {
      validationErrors.add("Horse name is too short");
    }
    return validationErrors;
  }

  private List<String> validateHeight(float height) {
    List<String> validationErrors = new ArrayList<>();
    if (height > 3) {
      validationErrors.add("Horse height is too large");
    }
    if (height <= 0) {
      validationErrors.add("Horse height is too small");
    }
    return validationErrors;
  }

  private List<String> validateWeight(float weight) {
    List<String> validationErrors = new ArrayList<>();
    if (weight > 2000) {
      validationErrors.add("Horse weight is too big");
    }
    if (weight <= 0) {
      validationErrors.add("Horse weight is too small");
    }
    return validationErrors;
  }

  private List<String> validateBirthdateNotInFuture(LocalDate date) {
    List<String> validationErrors = new ArrayList<>();
    if (date.isAfter(LocalDate.now()) || date.isEqual(LocalDate.now())) {
      validationErrors.add("Invalid date of birth");
    }
    return validationErrors;
  }
}