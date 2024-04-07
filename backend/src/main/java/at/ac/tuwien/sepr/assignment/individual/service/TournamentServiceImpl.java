package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailsParticipantsWithPointsDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailParticipantDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.entity.Standing;
import at.ac.tuwien.sepr.assignment.individual.entity.Tournament;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentStandingsTreeDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.mapper.TournamentMapper;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseMappedToTournamentDao;
import at.ac.tuwien.sepr.assignment.individual.persistence.TournamentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
  private final HorseMappedToTournamentDao horseMappedToTournamentDao;
  private final TournamentMapper mapper;
  private final TournamentValidator validator;
  private final TournamentMapper tournamentMapper;

  public TournamentServiceImpl(TournamentDao tournamentDao,
                               HorseDao horseDao,
                               HorseMappedToTournamentDao horseMappedToTournamentDao,
                               TournamentMapper mapper,
                               TournamentValidator validator, TournamentMapper tournamentMapper) {
    this.tournamentDao = tournamentDao;
    this.horseDao = horseDao;
    this.horseMappedToTournamentDao = horseMappedToTournamentDao;
    this.mapper = mapper;
    this.validator = validator;
    this.tournamentMapper = tournamentMapper;
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
      try { // Checks if horse doesn't exist (ConflictException)
        Horse horse = horseDao.getById(tournament.participants()[i].id()); // will throw NotFoundException if horse not found
        horses[i] = horse;
      } catch (NotFoundException e) {
        LOG.debug("horse {} not found", tournament.participants()[i].id());
        throw new ConflictException("Couldn't add horse " + tournament.participants()[i].name() + " to tournament because this horse doesn't exist",
            Collections.singletonList(e.getMessage()));
      }
    }
    LOG.debug("now adding to db: {}", tournament);
    Tournament newlyAddedTournament = tournamentDao.add(tournament);
    LOG.debug("This tournament was added to db: {}", newlyAddedTournament);
    for (int i = 0; i < tournament.participants().length; i++) {
      horseMappedToTournamentDao.add(tournament.participants()[i].id(), newlyAddedTournament.getId());
    }
    return mapper.entityToDetailDto(newlyAddedTournament, horses);
  }

  @Override
  public TournamentDetailDto getTournamentDetailsById(long id) throws NotFoundException {
    LOG.trace("getTournamentDetailsById({})", id);
    Tournament tournamentEntity = tournamentDao.getTournamentDetailsById(id);
    LOG.debug("retrieved the following tournament entry for the tournament id {}: ({})", id, tournamentEntity);
    List<Standing> horseStandings = horseMappedToTournamentDao.getHorsesInTournament(id);
    LOG.debug("retrieved the following horse to tournament mapping for the tournament id {}: ({})", id, horseStandings);
    TournamentDetailParticipantDto[] participants = new TournamentDetailParticipantDto[horseStandings.size()];
    for (int i = 0; i < horseStandings.size(); i++) {
      Horse horse = horseDao.getById(horseStandings.get(i).getHorseId());
      participants[i] = new TournamentDetailParticipantDto(
          horse.getId(),
          horse.getName(),
          horse.getDateOfBirth(),
          horseStandings.get(i).getEntryNumber(),
          horseStandings.get(i).getRoundReached()
          );
    }
    return new TournamentDetailDto(
      tournamentEntity.getId(),
      tournamentEntity.getName(),
      tournamentEntity.getStartDate(),
      tournamentEntity.getEndDate(),
      participants
    );
  }

  @Override
  public TournamentDetailDto updateTournament(TournamentUpdateDto tournamentUpdateDto) throws ValidationException, NotFoundException, ConflictException {
    LOG.trace("updateTournament({})", tournamentUpdateDto);
    TournamentDetailDto tournament = mapper.updateDtoToDetailDto(tournamentUpdateDto); // converts to this other dto to be able to reuse already existing code
    validator.validateForUpdate(tournament);
    final Tournament tournamentEntity = tournamentDao.getTournamentDetailsById(tournament.id()); // checks if the tournament doesn't exist (NotFoundException)
    LOG.debug("The provided tournament exists");
    Horse[] horses = new Horse[tournament.participants().length];

    // Checks if a horse doesn't exist (ConflictException)
    for (int i = 0; i < tournament.participants().length; i++) {
      try {
        Horse horse = horseDao.getById(tournament.participants()[i].horseId()); // will throw NotFoundException if horse was not found
        horses[i] = horse;
      } catch (NotFoundException e) {
        LOG.debug("horse {} not found", tournament.participants()[i].horseId());
        throw new ConflictException("Couldn't find a horse because this horse doesn't exist",
            Collections.singletonList(e.getMessage()));
      }
    }
    List<Standing> horseStandingsInTournament = horseMappedToTournamentDao.getHorsesInTournament(tournament.id());
    LOG.debug("All provided Horses exist in the database");

    // Checks if horse isn't in tournament
    for (int i = 0; i < horses.length; i++) {
      int finalI = i; // need this extra i to use in the lambda expression
      if (horseStandingsInTournament.stream().noneMatch(horseToTournamentMapping -> horseToTournamentMapping.getHorseId() == horses[finalI].getId()))  {
        throw new ConflictException("A provided horse isn't part of this tournament", Collections.singletonList("horse not found in mapping"));
      }
    }
    LOG.debug("All provided horses are in the database");

    // For the current horse-tournament-mapping in the db creates an array of horses which are at least in round 1
    boolean existingStandings = false; // is true if at least one horse exists that is in a round
    ArrayList<TournamentDetailParticipantDto> participants = new ArrayList<>();
    for (int i = 0; i < horses.length; i++) {
      int finalI = i;
      // Checks if the current horse is in any round
      Standing standingOfThisHorse = horseStandingsInTournament.stream().filter(mapping -> mapping.getHorseId() == horses[finalI].getId()).toList().getFirst();
      if (standingOfThisHorse.getRoundReached() != null) {
        existingStandings = true;
        participants.add(new TournamentDetailParticipantDto(
            horses[i].getId(),
            horses[i].getName(),
            horses[i].getDateOfBirth(),
            standingOfThisHorse.getEntryNumber(),
            standingOfThisHorse.getRoundReached()));
      }
    }

    // creating the tree for the current data in the database:
    TournamentStandingsTreeDto curTreeInDB = null;
    if (existingStandings == true) {
      LOG.debug("Existing standings found for this tournament");
      curTreeInDB = tournamentMapper.tournamentDetailsDtoToTournamentStandingTree(
          new TournamentDetailDto(
              tournamentEntity.getId(),
              tournamentEntity.getName(),
              tournamentEntity.getStartDate(),
              tournamentEntity.getEndDate(),
              participants.toArray(new TournamentDetailParticipantDto[participants.size()])));
      LOG.debug("This tree is currently in the database for tournament " + tournamentEntity.getId() + ": " + curTreeInDB.toStringSmaller());
    }

    // creating the tree for the new data to be saved in the database:
    participants = new ArrayList<>();
    for (int i = 0; i < horses.length; i++) {
      int finalI = i;
      List<TournamentDetailParticipantDto> horseOfNewTree =
          Arrays.stream(tournament.participants()).filter(horse -> horse.horseId() == horses[finalI].getId()).toList();
      Long newEntryNum;
      Long newRoundNum;
      if (horseOfNewTree.isEmpty()) {
        newEntryNum = null;
        newRoundNum = null;
      } else {
        newEntryNum = horseOfNewTree.getFirst().entryNumber();
        newRoundNum = horseOfNewTree.getFirst().roundReached();
      }
      participants.add(new TournamentDetailParticipantDto(
          horses[i].getId(),
          horses[i].getName(),
          horses[i].getDateOfBirth(),
          newEntryNum,
          newRoundNum));
    }
    TournamentStandingsTreeDto newTreeInDB = tournamentMapper.tournamentDetailsDtoToTournamentStandingTree(
        new TournamentDetailDto(
            tournamentEntity.getId(),
            tournamentEntity.getName(),
            tournamentEntity.getStartDate(),
            tournamentEntity.getEndDate(),
            participants.toArray(new TournamentDetailParticipantDto[participants.size()])));
    LOG.debug("This tree will be the new tree for the tournament " + tournamentEntity.getId() + ": " + newTreeInDB.toStringSmaller());
    if (!existingStandings) {
      LOG.debug("Since there is no standings tree in database, the new standings tree will be kept\n" + "The new standing tree will be:\n"
          + newTreeInDB.toStringSmaller());
    } else {
      LOG.debug("The new standingsTree will now be compared with the standingsTree in the database\n" + "\nThis is the new tree:\n"
          + newTreeInDB.toStringSmaller() + "\nIt will be compared to the existing tree:\n" + curTreeInDB.toStringSmaller());
      try {
        validator.validateTreeCompability(newTreeInDB, curTreeInDB);
      } catch (ValidationException e) {
        throw new ConflictException("The new standings tree is not compatible with the old one",
            Collections.singletonList(e.getMessage()));
      }
    }
    for (int i = 0; i < participants.size(); i++) {
      horseMappedToTournamentDao.update(participants.get(i), tournament.id());
    }
    return tournament;
  }

  @Override
  public TournamentDetailDto generateRound1ById(long id) throws NotFoundException, ConflictException {
    Tournament tournament = tournamentDao.getTournamentDetailsById(id); // throws NotFoundException if the tournament doesn't exist
    List<Standing> standings = horseMappedToTournamentDao.getHorsesInTournament(id); // throws NotFoundException if mappings for this tournament doesn't exist
    for (int i = 0; i < standings.size(); i++) { // throws ConflictException if horses are already in a round
      // tests if both entry and round number are set. If one is set, the other one should be set as well. But still tested both for good measure
      if (standings.get(i).getEntryNumber() != null || standings.get(i).getRoundReached() != null) {
        throw new ConflictException("Can't generate first round if horses are already placed in a round",
            Collections.singletonList("Found horse which is already placed in a round"));
      }
    }
    ArrayList<TournamentDetailsParticipantsWithPointsDto> horsesWithScores = new ArrayList<>(); // will contain the score for each horse
    for (Standing horse : standings) {
      List<Standing> allStandingsOfThisHorse; // contains all standings in all tournaments for a single horse
      allStandingsOfThisHorse = horseMappedToTournamentDao.getTournamentsForHorseInTimeFrame(
          horse.getHorseId(),
          tournament.getStartDate().minusMonths(12),
          tournament.getEndDate());
      long score = 0;
      for (int i = 0; i < allStandingsOfThisHorse.size(); i++) {
        if (allStandingsOfThisHorse.get(i).getRoundReached() != null) {
          switch ((int) allStandingsOfThisHorse.get(i).getRoundReached().longValue()) { // we check what round the horse reached in the current standing
            case 2: // Points are only awarded for round 2 and above
              score += 1;
              break;
            case 3:
              score += 3;
              break;
            case 4:
              score += 5;
              break;
            default:
              break;
          }
        }
      }
      // need to retrieve horse because the TournamentDetailsParticipantsWithPointsDto needs these basic infos which will later be returned to the user
      Horse horseEntity = horseDao.getById(horse.getHorseId());
      horsesWithScores.add(
          new TournamentDetailsParticipantsWithPointsDto(
              horseEntity.getId(),
              horseEntity.getName(),
              horseEntity.getDateOfBirth(),
              score
          ));
    }
    horsesWithScores.sort((horse1, horse2) -> {
      if (horse1.getPoints() == horse2.getPoints()) {
        return horse1.getName().compareTo(horse2.getName());
      } else {
        return Double.compare(horse1.getPoints(), horse2.getPoints()) == -1 ? 1 : -1; // we want to sort in descending order starting with the largest number
      }
    });
    long curEntryNumber = 0;
    for (int i = 0; i < horsesWithScores.size() / 2; i++) {
      horsesWithScores.get(i).setRoundReached(1L);
      horsesWithScores.get(i).setEntryNumber(curEntryNumber++);
      horsesWithScores.get(horsesWithScores.size() - 1 - i).setRoundReached(1L);
      horsesWithScores.get(horsesWithScores.size() - 1 - i).setEntryNumber(curEntryNumber++);
    }
    LOG.debug("created matching of the horses for round 1: ");
    horsesWithScores.stream().forEach(h -> LOG.debug(h.toString())); // for debugging purposes
    return mapper.participantsWithPointsDtoToDetailDto(tournament, horsesWithScores);
  }
}