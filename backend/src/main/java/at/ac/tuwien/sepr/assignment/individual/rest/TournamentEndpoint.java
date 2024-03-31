package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.service.TournamentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    } catch (ValidationException e) {
      // ValidationException will be rethrown to be handled by the ApplicationExceptionHandler
      throw e;
    } catch (FatalException e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      logClientError(status, "Couldn't execute database query with the following search parameters (" + searchParameters + ")", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (Exception e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      logClientError(status, "An unexpected error occurred when searching for tournaments with these search parameters (" + searchParameters + ")", e);
      // We don't display the error message of the unexpected Exception e to the user since it could possibly display too much information to the user.
      throw new ResponseStatusException(status, "An unexpected error occurred", e);
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


