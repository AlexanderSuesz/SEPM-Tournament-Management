package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.service.HorseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * REST controller for handling requests related to horses.
 * Provides endpoints for searching, adding, updating, and deleting horses.
 */
@RestController
@RequestMapping(path = HorseEndpoint.BASE_PATH)
public class HorseEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/horses";

  private final HorseService service;

  public HorseEndpoint(HorseService service) {
    this.service = service;
  }

  /**
   * Handles HTTP GET requests to search for horses based on the provided search parameters.
   *
   * @param searchParameters the search parameters for filtering horses
   * @return a stream of HorseListDto objects representing the filtered horses
   * @throws ValidationException if the search data is invalid
   */
  @GetMapping
  public Stream<HorseListDto> searchHorses(HorseSearchDto searchParameters) throws ValidationException {
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
      logClientError(status, "An unexpected error occurred when searching for horses with these search parameters (" + searchParameters + ")", e);
      // We don't display the error message of the unexpected Exception e to the user since it could possibly display too much information to the user.
      throw new ResponseStatusException(status, "An unexpected error occurred", e);
    }
  }

  /**
   * Handles HTTP GET requests to retrieve details of a specific horse by its ID.
   *
   * @param id the ID of the horse to retrieve
   * @return a HorseDetailDto representing the details of the requested horse
   */
  @GetMapping("{id}")
  public HorseDetailDto getById(@PathVariable("id") long id) {
    LOG.info("GET " + BASE_PATH + "/{}", id);
    try {
      return service.getById(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse with id " + id + " not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (FatalException e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      logClientError(status, "Couldn't retrieve one horse with id " + id + " from database", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (Exception e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      logClientError(status, "An unexpected error occurred when searching for the horse with the id " + id, e);
      // We don't display the error message of the unexpected Exception e to the user since it could possibly display too much information to the user.
      throw new ResponseStatusException(status, "An unexpected error occurred", e);
    }
  }


  /**
   * Handles HTTP POST requests to add a new horse.
   *
   * @param toAdd the HorseDetailDto containing the details of the horse to add
   * @return a HorseDetailDto representing the details of the added horse
   * @throws ValidationException if the provided data for the new horse is invalid
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public HorseDetailDto add(@RequestBody HorseDetailDto toAdd) throws ValidationException {
    LOG.info("POST " + BASE_PATH);
    LOG.debug("Body of request:\n{}", toAdd);
    try {
      return service.add(toAdd);
    } catch (ValidationException e) {
      // ValidationException will be rethrown to be handled by the ApplicationExceptionHandler
      throw e;
    } catch (FatalException e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      logClientError(status, "There was an error when adding the horse " + toAdd + " to the database", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (Exception e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      logClientError(status, "An unexpected error occurred when adding the horse " + toAdd + " to the database", e);
      // We don't display the error message of the unexpected Exception e to the user since it could possibly display too much information to the user.
      throw new ResponseStatusException(status, "An unexpected error occurred", e);
    }
  }

  /**
   * Handles HTTP PUT requests to update an existing horse.
   *
   * @param id       the ID of the horse to update
   * @param toUpdate the HorseDetailDto containing the updated details of the horse
   * @return a HorseDetailDto representing the details of the updated horse
   * @throws ValidationException if the provided data for the updated horse is invalid
   * @throws ConflictException   if there is a conflict while updating the horse
   */
  @PutMapping("{id}")
  public HorseDetailDto update(@PathVariable("id") long id, @RequestBody HorseDetailDto toUpdate) throws ValidationException, ConflictException {
    LOG.info("PUT " + BASE_PATH + "/{}", toUpdate);
    LOG.debug("Body of request:\n{}", toUpdate);
    try {
      return service.update(toUpdate.withId(id));
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to update not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (ConflictException e) {
      HttpStatus status = HttpStatus.CONFLICT;
      logClientError(status, "There was a conflict when updating a horse's data", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (ValidationException e) {
      // ValidationException will be rethrown to be handled by the ApplicationExceptionHandler
      throw e;
    } catch (FatalException e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      logClientError(status, "There was an error when updating the horse with this data (" + toUpdate + ")", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (Exception e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      logClientError(status, "An unexpected error occurred when updating the horse with this data (" + toUpdate + ")", e);
      // We don't display the error message of the unexpected Exception e to the user since it could possibly display too much information to the user.
      throw new ResponseStatusException(status, "An unexpected error occurred", e);
    }
  }

  /**
   * Handles HTTP DELETE requests to delete a horse by its ID.
   *
   * @param id the ID of the horse to delete
   */
  @DeleteMapping("{id}")
  public void deleteById(@PathVariable("id") long id) {
    LOG.info("Delete " + BASE_PATH + "/{}", id);
    try {
      service.deleteById(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "No horse with id " + id + " found and therefore could not be deleted", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (FatalException e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      logClientError(status, "Could not delete horse with id " + id, e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (Exception e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      logClientError(status, "An unexpected error occurred when deleting the horse with the id " + id, e);
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
