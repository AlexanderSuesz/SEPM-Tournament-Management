package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator class for performing validation checks on tournament data.
 * This class ensures that the data provided for a tournament entity is valid.
 */

@Component
public class TournamentValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Validates the provided search parameters before performing the search operation (searching for the tournaments in the persistence storage).
   *
   * @param searchParameters the search parameters after which tournaments should be selected
   * @throws ValidationException if validation fails due to invalid data
   */
  public void validateForSearch(TournamentSearchDto searchParameters) throws ValidationException {
    LOG.trace("validateForSearch({})", searchParameters);
    List<String> validationErrors = new ArrayList<>();
    if (searchParameters.limit() != null && searchParameters.limit() < 0) {
      validationErrors.add("There can be can't be less than 0 tournaments displayed");
    }
    if (searchParameters.earliestTournamentDay() != null && searchParameters.latestTournamentDay() != null
        && searchParameters.earliestTournamentDay().isAfter(searchParameters.latestTournamentDay())) {
      validationErrors.add("Earliest tournament date needs to be before latest tournament date");
    }
    if (searchParameters.name() != null) {
      validationErrors.addAll(validateName(searchParameters.name()));
    }
    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of search parameters failed", validationErrors);
    }
  }

  private List<String> validateName(String name) {
    List<String> validationErrors = new ArrayList<>();
    if (name == null) {
      validationErrors.add("Tournament name is too short"); // can't check for the rest if is null
      return validationErrors;
    } else if (name.length() <= 0) {
      validationErrors.add("Tournament name is too short");
    } else if (name.length() > 100) {
      validationErrors.add("Tournament name is too long");
    }
    return validationErrors;
  }
}
