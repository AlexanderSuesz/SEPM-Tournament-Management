package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.entity.Standing;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;

/**
 * Data Access Object for the mapping of horses to tournaments together with the horse's standing in the tournament.
 * Implements access functionality to the application's persistent data store regarding the horse_mapped_to_tournament which is the table of described mapping.
 */
public interface HorseMappedToTournamentDao {

  /**
   * Counts the amount of tournaments the given horse participates in.
   *
   * @param horseId the horse id for which to count the tournaments
   * @return then number of tournaments this horse participates in
   */
  long countTournamentsForHorse(long horseId);

  /**
   * Counts how often a single horse to tournament mapping exist in the database (it can either exist (1) or it doesn't (0)).
   *
   * @param horseId the id of the horse of which the mapping to the tournament should be counted
   * @param tournamentId the id of the tournament of which the mapping to the horse should be counted
   * @return the occurrence count of the given mapping in the database
   */
  int countOccurrenceOfEntry(long horseId, long tournamentId);

  /**
   * Add the horse id mapped to the tournament id in the persistent data store.
   *
   * @param horseId the id of the horse which will be mapped to the tournament
   * @param tournamentId the id of the tournament which will be mapped to the horse
   * @return the new Standing for the horse in this tournament.
   * @throws ConflictException if there already exists a mapping for this horse and tournament
   */
  Standing add(long horseId, long tournamentId) throws ConflictException;
}