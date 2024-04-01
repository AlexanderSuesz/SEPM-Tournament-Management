package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;

import java.util.stream.Stream;

/**
 * Service for working with horses.
 */
public interface HorseService {
  /**
   * Search for horses in the persistent data store matching all provided fields.
   * The name is considered a match, if the search string is a substring of the field in Horse.
   *
   * @param searchParameters the search parameters to use in filtering.
   * @return the horses where the given fields match.
   * @throws ValidationException if the search data is in itself incorrect (name too long, born Earliest > born Latest …)
   */
  Stream<HorseListDto> search(HorseSearchDto searchParameters) throws ValidationException;

  /**
   * Updates the horse with the ID given in {@code horse}
   * with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws NotFoundException   if the horse with given ID does not exist in the persistent data store
   * @throws ValidationException if the update data given for the horse is in itself incorrect (no name, name too long …)
   * @throws ConflictException   if the update data given for the horse is in conflict with the data currently in the system (breed does not exist, …)
   */
  HorseDetailDto update(HorseDetailDto horse) throws NotFoundException, ValidationException, ConflictException;


  /**
   * Get the horse with given ID, with more detail information.
   * This includes the breed of the horse.
   *
   * @param id the ID of the horse to get
   * @return the horse with ID {@code id}
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  HorseDetailDto getById(long id) throws NotFoundException;

  /**
   * Adds the horse in the persistent data store.
   *
   * @param horse the horse to add
   * @return the added horse
   * @throws ValidationException if the data of the new horse is in itself incorrect (no name, name too long, …)
   */
  HorseDetailDto add(HorseDetailDto horse) throws ValidationException;

  /**
   * Deletes the horse with given ID.
   *
   * @param id the ID of the horse to delete
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  void deleteById(long id) throws NotFoundException;
}
