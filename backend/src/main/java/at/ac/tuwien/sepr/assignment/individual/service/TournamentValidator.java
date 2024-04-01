package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
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
  private final HorseValidator horseValidator;

  public TournamentValidator(HorseValidator horseValidator) {
    this.horseValidator = horseValidator; // needed to validate the horses used as arguments when a tournament is created
  }

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
      validationErrors.add("There can't be less than 0 tournaments displayed");
    }
    if (searchParameters.earliestTournamentDay() != null && searchParameters.latestTournamentDay() != null
        && searchParameters.earliestTournamentDay().isAfter(searchParameters.latestTournamentDay())) {
      validationErrors.add("Earliest tournament date needs to be before latest tournament date");
    }
    if (searchParameters.name() != null) {
      validationErrors.addAll(validateName(searchParameters.name()));
    }
    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of the search parameters failed", validationErrors);
    }
  }

  /**
   * Validates the provided tournament details before performing an insert operation (adding a new tournament to the persistence storage).
   *
   * @param tournament the tournament details to validate
   * @throws ValidationException if validation fails due to invalid data
   */
  public void validateForInsert(TournamentCreateDto tournament) throws ValidationException {
    LOG.trace("validateForInsert({})", tournament);
    List<String> validationErrors = new ArrayList<>();
    validationErrors.addAll(validateName(tournament.name()));
    boolean validStartDate = true;
    if (tournament.startDate() == null) {
      validStartDate = false;
      validationErrors.add("No start date was picked for the tournament");
    }
    if (tournament.endDate() == null) {
      validationErrors.add("No end date was picked for the tournament");
    }
    if (tournament.startDate() != null && tournament.endDate() != null
        && tournament.startDate().isAfter(tournament.endDate())) {
      validationErrors.add("start date of tournament needs to be before its end date");
    }
    if (tournament.participants() == null || tournament.participants().length != 8) {
      validationErrors.add("A tournament must have exactly 8 horses");
    }
    if (tournament.participants() != null) { // tournament.participants().length can only be accessed if horses is not null
      ArrayList<Long> horsesComparison = new ArrayList<>(); // used to check if the same horse was added multiple times to the tournament
      for (int i = 0; i < tournament.participants().length; i++) {
        if (tournament.participants()[i] == null) {
          validationErrors.add("A horse containing no data was provided");
        } else {
          if (horsesComparison.contains(tournament.participants()[i].id())) {
            validationErrors.add("The same horse " + tournament.participants()[i].name() + " appears multiple times in this tournament");
          }
          horsesComparison.add(tournament.participants()[i].id());
          if (validStartDate) {
            // The compatibility of the horses with the tournament should only be tested if the tournament has a start date
            try {
              horseValidator.validateForCompatibilityWithTournament(
                  tournament.participants()[i],
                  tournament.startDate()
              );
            } catch (ValidationException e) {
              validationErrors.addAll(e.errors());
            }
          }
        }
      }
    }
    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of the tournament failed", validationErrors);
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
