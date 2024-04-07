package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailParticipantDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentStandingsTreeDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.mapper.TournamentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
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
      validationErrors.addAll(validateNameWithoutRegex(searchParameters.name()));
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
    validationErrors.addAll(validateNameWithRegex(tournament.name()));
    validationErrors.addAll(validateDates(tournament.startDate(), tournament.endDate()));
    boolean validStartDate = true;
    if (tournament.startDate() == null) {
      validStartDate = false; // will skip the horse checks for which the date is needed
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

  /**
   * Validates the provided tournament details before performing an update operation (updating a tournament in the persistence storage).
   *
   * @param tournament the tournament to validate
   * @throws ValidationException if validation fails due to invalid data
   */
  public void validateForUpdate(TournamentDetailDto tournament) throws ValidationException {
    LOG.trace("validateForUpdate({})", tournament);
    List<String> validationErrors = new ArrayList<>();
    if (tournament.participants() == null || tournament.participants().length > 8) {
      validationErrors.add("A tournament can only have between 0 and 8 horses currently competing against each other");
    }
    if (tournament.participants() != null) { // tournament.participants().length can only be accessed if horses are not null
      ArrayList<Long> horsesIdComparison = new ArrayList<>(); // used to check if the same horse was added multiple times to the tournament
      ArrayList<Long> horsesEntryNumberComparison = new ArrayList<>(); // used to check if the same entry number was added multiple times to the tournament
      int horsesInRound1 = 0;
      int horsesInRound2 = 0;
      int horsesInRound3 = 0;
      int horsesInRound4 = 0;
      for (int i = 0; i < tournament.participants().length; i++) {
        if (tournament.participants()[i] == null) {
          validationErrors.add("A horse containing no data was provided");
        } else {
          if (horsesIdComparison.contains(tournament.participants()[i].horseId())) {
            validationErrors.add("The same horse appears multiple times in this tournament");
          }
          if (tournament.participants()[i].entryNumber() != null && (tournament.participants()[i].entryNumber() < 0
              || tournament.participants()[i].entryNumber() > 7)) {
            validationErrors.add("The entry number for horses can only be between 0 and 7");
          }
          if (tournament.participants()[i].roundReached() != null && (tournament.participants()[i].roundReached() < 1
              || tournament.participants()[i].roundReached() > 4)) {
            validationErrors.add("A round must be between 1 and 4");
          }
          if (tournament.participants()[i].roundReached() != null) {
            switch ((int) tournament.participants()[i].roundReached().longValue()) {
              case 1:
                horsesInRound1++;
                break;
              case 2:
                horsesInRound2++;
                break;
              case 3:
                horsesInRound3++;
                break;
              case 4:
                horsesInRound4++;
                break;
              default:
                validationErrors.add("An invalid round number " + tournament.participants()[i].roundReached() + " was found");
            }
          }
          horsesIdComparison.add(tournament.participants()[i].horseId());
          horsesEntryNumberComparison.add(tournament.participants()[i].entryNumber());
        }
      }
      if (horsesInRound1 < 0 || horsesInRound1 > 8) {
        validationErrors.add("There can only be 8 horses in round 1");
      }
      if (horsesInRound2 < 0 || horsesInRound2 > 4) {
        validationErrors.add("There can only be 4 horses in round 2");
      }
      if (horsesInRound3 < 0 || horsesInRound3 > 2) {
        validationErrors.add("There can only be 2 horses in round 3");
      }
      if (horsesInRound4 < 0 || horsesInRound4 > 1) {
        validationErrors.add("There can only be 1 horse in round 4");
      }
    }
    TournamentStandingsTreeDto tree = new TournamentMapper().tournamentDetailsDtoToTournamentStandingTree(tournament);
    validationErrors.addAll(validateTreeStructure(4, 0, 7, tree));
    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of the tournament failed", validationErrors);
    }
  }

  private List<String> validateTreeStructure(long curRound, long startEntryNumber, long endEntryNumber, TournamentStandingsTreeDto tournamentTree) {
    List<String> validationErrors = new ArrayList<>();
    if (curRound <= 1) {
      return validationErrors;
    }
    if (tournamentTree.getThisParticipant() != null) {
      if (tournamentTree.getThisParticipant().entryNumber() < startEntryNumber || tournamentTree.getThisParticipant().entryNumber() > endEntryNumber) {
        validationErrors.add("The horse " + tournamentTree.getThisParticipant().name() + " is in an invalid position in the tree (opposite site of expected)");
        LOG.debug("The horse " + tournamentTree.getThisParticipant().horseId() + " has the entry number " + tournamentTree.getThisParticipant().entryNumber()
            + " and it's number is not between [" + startEntryNumber + ", " + endEntryNumber + "]");
      }
      if (tournamentTree.getBranches().getFirst().getThisParticipant() != null && tournamentTree.getBranches().getLast().getThisParticipant() != null
          && tournamentTree.getBranches().getFirst().getThisParticipant().horseId() != tournamentTree.getThisParticipant().horseId()
          && tournamentTree.getBranches().getLast().getThisParticipant().horseId() != tournamentTree.getThisParticipant().horseId()) {

        validationErrors.add("Unexpected child horses for the winner " + tournamentTree.getThisParticipant().name() + " of the "
            + tournamentTree.getThisParticipant().roundReached() + " round");
      }
      if (tournamentTree.getBranches().getFirst().getThisParticipant() == null && tournamentTree.getBranches().getLast().getThisParticipant() == null) {
        validationErrors.add("The winning horse " + tournamentTree.getThisParticipant().name() + " of round "
            + tournamentTree.getThisParticipant().roundReached() + " had no opponent in the previous round");
      }
    }
    long split = (endEntryNumber - startEntryNumber) / 2 + startEntryNumber;
    validationErrors.addAll(validateTreeStructure(curRound - 1, 0, split,
        tournamentTree.getBranches().get(0)));
    validationErrors.addAll(validateTreeStructure(curRound - 1, split + 1, endEntryNumber,
        tournamentTree.getBranches().get(1)));
    return validationErrors;
  }

  /**
   * Checks if the current tree curTree compatible is with the new tree structure newTree
   */
  public void validateTreeCompability(TournamentStandingsTreeDto newTree, TournamentStandingsTreeDto curTree) throws ValidationException {
    LOG.trace("validateTreeCompability({}, {})", newTree, curTree);
    List<String> validationErrors = new ArrayList<>();
    validationErrors.addAll(treeNodeComperator(newTree, curTree));
    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of compatibility of the the new with the current tree structure failed", validationErrors);
    }
  }

  private List<String> treeNodeComperator(TournamentStandingsTreeDto newTreeNode, TournamentStandingsTreeDto curTreeNode) {
    List<String> validationErrors = new ArrayList<>();
    if (curTreeNode.getBranches() == null) {
      return validationErrors;
    }
    if (curTreeNode.getThisParticipant() != null) {
      TournamentDetailParticipantDto childOfLeftBranchNew = newTreeNode.getBranches().getFirst().getThisParticipant();
      TournamentDetailParticipantDto childOfRightBranchNew = newTreeNode.getBranches().getLast().getThisParticipant();
      TournamentDetailParticipantDto childOfLeftBranchCur = curTreeNode.getBranches().getFirst().getThisParticipant();
      TournamentDetailParticipantDto childOfRightBranchCur = curTreeNode.getBranches().getLast().getThisParticipant();
      if (
          !(
              (childOfLeftBranchCur == null && childOfLeftBranchNew == null)
                  || (childOfLeftBranchCur != null && childOfLeftBranchNew != null && childOfLeftBranchCur.horseId() == childOfLeftBranchNew.horseId())
          )
      ) {
        validationErrors.add("Found an inconsistency where horses of previously completed rounds don't match the already existing standing");
      }
      if (
          !(
              (childOfRightBranchCur == null && childOfRightBranchNew == null)
                  || (childOfRightBranchCur != null && childOfRightBranchNew != null && childOfRightBranchCur.horseId() == childOfRightBranchNew.horseId())
          )
      ) {
        validationErrors.add("Found an inconsistency where horses of previously completed rounds don't match the already existing standing");
      }
    }
    validationErrors.addAll(treeNodeComperator(newTreeNode.getBranches().getFirst(), curTreeNode.getBranches().getFirst()));
    validationErrors.addAll(treeNodeComperator(newTreeNode.getBranches().getLast(), curTreeNode.getBranches().getLast()));
    return validationErrors;
  }

  /**
   * Validates the name of the tournament same as validateNameWithoutRegex but additionally checks the string content for special characters.
   *
   * @param name the name of the tournament
   * @return all validation errors which occurred
   */
  private List<String> validateNameWithRegex(String name) {
    List<String> validationErrors = new ArrayList<>();
    validationErrors.addAll(validateNameWithoutRegex(name));
    if (name != null && !name.matches("^[0-9a-zA-Z _]+")) {
      validationErrors.add("The tournament name can only consist of numbers, letters and the special characters space and underscore");
    }
    return validationErrors;
  }


  /**
   * Validates the name of the tournament without checking the string content for special characters.
   * Mainly using this validation method when searching for tournaments by their name.
   * This is to e.g. not constantly throw errors for the user when a special character is typed in the search bar.
   *
   * @param name the name of the tournament
   * @return all validation errors which occurred
   */
  private List<String> validateNameWithoutRegex(String name) {
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

  private List<String> validateDates(LocalDate startDate, LocalDate endDate) {
    List<String> validationErrors = new ArrayList<>();
    if (startDate == null) {
      validationErrors.add("No start date was picked for the tournament");
    }
    if (endDate == null) {
      validationErrors.add("No end date was picked for the tournament");
    }
    if (startDate != null && endDate != null
        && startDate.isAfter(endDate)) {
      validationErrors.add("start date of tournament needs to be before its end date");
    }
    return validationErrors;
  }

}
