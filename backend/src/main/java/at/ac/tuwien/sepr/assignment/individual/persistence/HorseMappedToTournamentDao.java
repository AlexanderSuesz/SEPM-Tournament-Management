package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailParticipantDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Standing;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;

import java.util.List;

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
   * Retrieves the ID of every horse which takes part in this tournament (represented by the given tournamentId)
   *
   * @param tournamentId the id of the tournament of which the horses should be retrieved
   * @return a list of all standings of the horses taking part in this tournament
   * @throws NotFoundException if no horse to tournament mapping was found
   */
  List<Standing> getHorsesInTournament(long tournamentId) throws NotFoundException;

  /**
   * Add the horse id mapped to the tournament id in the persistent data store.
   *
   * @param horseId the id of the horse which will be mapped to the tournament
   * @param tournamentId the id of the tournament which will be mapped to the horse
   * @return the new Standing for the horse in this tournament.
   * @throws ConflictException if there already exists a mapping for this horse and tournament
   */
  Standing add(long horseId, long tournamentId) throws ConflictException;

  /**
   * Updates the horse mapped to the tournament in the persistent data storage.
   *
   * @param horseTournamentDetails the details of the horse related to the tournament
   * @param tournamentId the identifier of the tournament
   * @return the new Standing for the horse in this tournament.
   * @throws NotFoundException if this horse to tournament mapping doesn't exist
   */
  Standing update(TournamentDetailParticipantDto horseTournamentDetails, long tournamentId) throws NotFoundException;

  /**
   * Retrieves the Standing of the horse with the id horseId which takes part in this tournament with the id tournamentId
   *
   * @param horseId the id of the horse
   * @param tournamentId the id of the tournament
   * @return the Standing for this horse in this tournament
   * @throws NotFoundException if there was no Standing found for this horse in this tournament
   */
  Standing getSingleMapping(long horseId, long tournamentId) throws NotFoundException;
}
