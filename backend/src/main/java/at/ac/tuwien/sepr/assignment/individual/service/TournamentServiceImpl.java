package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.entity.Tournament;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.mapper.TournamentMapper;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.persistence.TournamentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * Service class providing operations related to tournament entities.
 * This class implements the TournamentService interface.
 */
@Service
public class TournamentServiceImpl implements TournamentService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final TournamentDao tournamentDao;
  private final HorseDao horseDao; // Necessary for checking for ConflictException when adding horses to a tournament
  private final TournamentMapper mapper;
  private final TournamentValidator validator;

  public TournamentServiceImpl(TournamentDao tournamentDao, HorseDao horseDao, TournamentMapper mapper, TournamentValidator validator) {
    this.tournamentDao = tournamentDao;
    this.horseDao = horseDao;
    this.mapper = mapper;
    this.validator = validator;
  }

  @Override
  public Stream<TournamentListDto> search(TournamentSearchDto searchParameters) throws ValidationException {
    LOG.trace("search({})", searchParameters);
    validator.validateForSearch(searchParameters);
    var tournaments = tournamentDao.search(searchParameters);
    LOG.debug("tournaments found: {}", tournaments);
    return tournaments.stream().map(tournament -> mapper.entityToListDto(tournament));
  }

  @Override
  public TournamentDetailDto add(TournamentCreateDto tournament) throws ValidationException, ConflictException {
    LOG.trace("add({})", tournament);
    validator.validateForInsert(tournament);
    Horse[] horses = new Horse[tournament.participants().length];
    for (int i = 0; i < tournament.participants().length; i++) {
      try {
        Horse horse = horseDao.getById(tournament.participants()[i].id()); // checks for conflict exception and is used to create the TournamentDetailDto
        horses[i] = horse;
      } catch (NotFoundException e) {
        LOG.debug("horse {} not found", tournament.participants()[i].id());
        throw new ConflictException("Couldn't add horse " + tournament.participants()[i].name() + " to tournament because this horse doesn't exist",
            Collections.singletonList(e.getMessage()));
      }
    }
    LOG.debug("now adding to db: {}", tournament);
    Tournament newlyAddedTournament = tournamentDao.add(tournament);

    return mapper.entityToDetailDto(newlyAddedTournament, horses);
  }
}