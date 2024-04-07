package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.service.TournamentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

/**
 * REST controller for handling requests related to tournaments.
 * Provides endpoints for searching, adding and updating tournaments.
 */
@RestController
@RequestMapping(path = TournamentEndpoint.BASE_PATH)
public class TournamentEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/tournaments";
  private final TournamentService service;

  public TournamentEndpoint(TournamentService service) {
    this.service = service;
  }

  /**
   * Handles HTTP GET requests to search for tournaments based on the provided search parameters.
   *
   * @param searchParameters the search parameters for filtering tournaments
   * @return a stream of TournamentListDto objects representing the filtered tournaments
   * @throws ValidationException if the search data is invalid
   */
  @GetMapping
  public Stream<TournamentListDto> searchTournaments(TournamentSearchDto searchParameters) throws ValidationException {
    LOG.info("GET " + BASE_PATH);
    LOG.debug("request parameters: {}", searchParameters);
    try {
      return service.search(searchParameters);
    } catch (FatalException e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      logClientError(status, "Couldn't execute database query with the following search parameters (" + searchParameters + ")", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  /**
   * Handles HTTP GET requests to retrieve details of a specific tournament by its ID.
   *
   * @param id ID of the tournament
   * @return a TournamentDetailDto representing the details of the requested tournament
   */
  @GetMapping("/standings/{id}")
  public TournamentDetailDto getTournamentDetailsById(@PathVariable("id") long id) {
    LOG.info("GET " + BASE_PATH + "/standings/{}", id);
    try {
      TournamentDetailDto tournamentDetails = service.getTournamentDetailsById(id);
      LOG.debug("The following tournament details will be sent to /standings/{} : ({})", id, tournamentDetails);
      return tournamentDetails;
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "No tournament with the id " + id + " found in the database", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (FatalException e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      logClientError(status, "There was an error when retrieving the data of the tournament with id " + id + " from the database", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  /**
   * Handles HTTP GET requests to retrieve a generated standing for the horses for round 1 of the given tournament.
   *
   * @param id the id of the tournament
   * @return the new tournament details including the generated first round standings of all horses
   */
  @GetMapping("/standings/generate/{id}")
  public TournamentDetailDto generateRound1ById(@PathVariable("id") long id) {
    LOG.info("GET " + BASE_PATH + "/standings/generate/{}", id);
    try {
      return service.generateRound1ById(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "No tournament with the id " + id + " found in the database", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (ConflictException e) {
      HttpStatus status = HttpStatus.CONFLICT;
      logClientError(status, "There was a conflict when generating the first round for the horses of the tournament " + id, e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (FatalException e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      logClientError(status, "There was an error when retrieving the data of the tournament with id " + id + " from the database", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  /**
   * Handles HTTP PUT requests to update details of a specific tournament.
   *
   * @param tournamentUpdateDto the new data of the tournament which should replace the old data
   * @return a TournamentDetailDto representing the current details of the tournament
   */
  @PutMapping("/standings/{id}")
  public TournamentDetailDto updateTournamentStandings(@RequestBody TournamentUpdateDto tournamentUpdateDto) throws ValidationException {
    LOG.info("PUT " + BASE_PATH + "/standings/{}", tournamentUpdateDto.id());
    LOG.debug("Body of request: [{}, {}]", tournamentUpdateDto.id(), tournamentUpdateDto.participants());
    try {
      return service.updateTournament(tournamentUpdateDto);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "No tournament with the id " + tournamentUpdateDto.id() + " found in the database", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (ConflictException e) {
      HttpStatus status = HttpStatus.CONFLICT;
      logClientError(status, "There was a conflict when adding the tournament (a horse which takes part in the tournament is in conflict with the "
          + "existing horses in the database)", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (FatalException e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      logClientError(status, "There was an error when updating the tournament with the new data (" + tournamentUpdateDto + ")", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  /**
   * Handles HTTP POST requests to add a new tournament.
   *
   * @param toAdd the TournamentCreateDto containing the details of the tournament to add
   * @return a TournamentDetailDto representing the details of the added tournament
   * @throws ValidationException if the provided data for the new tournament is invalid
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TournamentDetailDto add(@RequestBody TournamentCreateDto toAdd) throws ValidationException {
    LOG.info("POST " + BASE_PATH + "/create");
    LOG.debug("Body of request:\n{}", toAdd);
    try {
      return service.add(toAdd);
    } catch (ConflictException e) {
      HttpStatus status = HttpStatus.CONFLICT;
      logClientError(status, "There was a conflict when adding the tournament (a horse which takes part in the tournament doesn't exist)", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (FatalException e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      logClientError(status, "There was an error when adding the tournament " + toAdd + " to the database", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  private void logClientError(HttpStatus status, String message, Exception e) {
    if (status != HttpStatus.INTERNAL_SERVER_ERROR) { // when an expected error occurs, then it should be logged with 'warn'
      LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
    } else { // when a terrible unexpected error occurs, then it should be logged with 'error'
      LOG.error("{} {}: {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage(), e.getStackTrace());
    }
  }
}


