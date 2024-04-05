package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.dto.BreedDto;
import at.ac.tuwien.sepr.assignment.individual.dto.BreedSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.service.BreedService;
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
 * REST controller for handling requests related to breeds.
 * Provides endpoints for searching breeds.
 */
@RestController
@RequestMapping(path = BreedEndpoint.BASE_PATH)
public class BreedEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public static final String BASE_PATH = "/breeds";

  private final BreedService service;

  public BreedEndpoint(BreedService service) {
    this.service = service;
  }

  /**
   * Handles HTTP GET requests to search for breeds based on the provided search parameters.
   *
   * @param searchParams the search parameters for filtering breeds
   * @return a stream of BreedDto objects matching the search criteria
   */
  @GetMapping
  public Stream<BreedDto> search(BreedSearchDto searchParams) {
    LOG.info("GET " + BASE_PATH);
    LOG.debug("Request Params: {}", searchParams);
    try {
      return service.search(searchParams);
    } catch (FatalException e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      logClientError(status, "Couldn't execute database query with these search parameters (" + searchParams + ")", e);
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
