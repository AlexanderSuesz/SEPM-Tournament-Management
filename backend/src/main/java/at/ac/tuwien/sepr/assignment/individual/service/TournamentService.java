package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;

import java.util.stream.Stream;

/**
 * Service for working with tournaments.
 */
public interface TournamentService {
  /**
   *  Search for tournaments in the persistent data store matching all provided fields.
   *  The name is considered a match, if the search string is a substring of the field in tournament.
   *
   * @param searchParameters the search parameters to use in filtering.
   * @return the tournaments where the given fields match.
   * @throws ValidationException if the search data is in itself incorrect (name too long, start date > end date)
   */
  Stream<TournamentListDto> search(TournamentSearchDto searchParameters) throws ValidationException;

  /**
   * Adds the tournament in the persistent data store.
   *
   * @param tournament the tournament to add
   * @return the added tournament
   * @throws ValidationException if the data of the new tournament is in itself incorrect (no name, name too long, …)
   * @throws ConflictException if the tournament data is in conflict with the data currently in the system (horse in tournament doesn't exist in database)
   */
  TournamentDetailDto add(TournamentCreateDto tournament) throws ValidationException, ConflictException;

  /**
   * Retrieves the details about a tournament with the given id.
   *
   * @param id the id of the tournament
   * @return the details of this tournament
   * @throws NotFoundException if no tournament with this id exists
   */
  TournamentDetailDto getTournamentDetailsById(long id) throws NotFoundException;

  /**
   * Updates an already existing tournament with the given tournament details.
   *
   * @param tournament the new data of the tournament which should replace the old data of the tournament
   * @return returns the current details of the tournament
   * @throws NotFoundException if this tournament was not found
   * @throws ConflictException if horses provided in tournament details don't match with the current details of this tournament (not expected horses, etc.)
   */
  TournamentDetailDto updateTournament(TournamentUpdateDto tournament) throws ValidationException, NotFoundException, ConflictException;

  /**
   * Retrieves the details of a tournament with id where the standing of each horse was generated for round 1 based on its score the past 12 months.
   *
   * @param id the id of the tournament
   * @return the generated horse standings of this tournament
   * @throws NotFoundException if the tournament was not found
   * @throws ConflictException if a horse is already in at least round 1
   */
  TournamentDetailDto generateRound1ById(long id) throws NotFoundException, ConflictException;
}