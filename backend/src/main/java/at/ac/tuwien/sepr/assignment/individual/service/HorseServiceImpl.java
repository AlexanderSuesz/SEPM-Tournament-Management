package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.BreedDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.mapper.HorseMapper;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseMappedToTournamentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service class providing operations related to horse entities.
 * This class implements the HorseService interface.
 */
@Service
public class HorseServiceImpl implements HorseService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final HorseDao horseDao;
  private final HorseMappedToTournamentDao horseMappedToTournamentDao;
  private final HorseMapper mapper;
  private final HorseValidator validator;
  private final BreedService breedService;

  public HorseServiceImpl(HorseDao horseDao,
                          HorseMappedToTournamentDao horseMappedToTournamentDao,
                          HorseMapper mapper,
                          HorseValidator validator,
                          BreedService breedService) {
    this.horseDao = horseDao;
    this.horseMappedToTournamentDao = horseMappedToTournamentDao;
    this.mapper = mapper;
    this.validator = validator;
    this.breedService = breedService;
  }

  @Override
  public Stream<HorseListDto> search(HorseSearchDto searchParameters) throws ValidationException {
    LOG.trace("search({})", searchParameters);
    validator.validateForSearch(searchParameters);
    var horses = horseDao.search(searchParameters);
    // First get all breed ids…
    var breeds = horses.stream()
        .map(Horse::getBreedId)
        .filter(Objects::nonNull)
        .collect(Collectors.toUnmodifiableSet());
    // … then get the breeds all at once.
    var breedsPerId = breedMapForHorses(breeds);

    return horses.stream()
        .map(horse -> mapper.entityToListDto(horse, breedsPerId));
  }


  @Override
  public HorseDetailDto update(HorseDetailDto horse) throws NotFoundException, ValidationException, ConflictException {
    LOG.trace("update({})", horse);
    validator.validateForUpdate(horse);
    var updatedHorse = horseDao.update(horse);
    var breeds = breedMapForSingleHorse(updatedHorse);
    return mapper.entityToDetailDto(updatedHorse, breeds);
  }


  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {
    LOG.trace("details({})", id);
    Horse horse = horseDao.getById(id);
    var breeds = breedMapForSingleHorse(horse);
    return mapper.entityToDetailDto(horse, breeds);
  }

  /**
   * Retrieves breed information for a single horse based on its breed ID.
   *
   * @param horse the horse for which breed information is to be retrieved
   * @return a map containing breed information for the specified horse
   */
  private Map<Long, BreedDto> breedMapForSingleHorse(Horse horse) {
    return breedMapForHorses(Collections.singleton(horse.getBreedId()));
  }

  /**
   * Retrieves breed information for multiple horses based on their breed IDs.
   *
   * @param horse a set containing the IDs of horses for which breed information is to be retrieved
   * @return a map containing breed information for the specified horses
   */
  private Map<Long, BreedDto> breedMapForHorses(Set<Long> horse) {
    return breedService.findBreedsByIds(horse)
        .collect(Collectors.toUnmodifiableMap(BreedDto::id, Function.identity()));
  }

  @Override
  public HorseDetailDto add(HorseDetailDto horse) throws ValidationException {
    LOG.trace("add({})", horse);
    validator.validateForInsert(horse);
    LOG.debug("now adding to db: {}", horse);
    Horse newlyAddedHorse = horseDao.add(horse);
    var breeds = breedMapForSingleHorse(newlyAddedHorse);
    return mapper.entityToDetailDto(newlyAddedHorse, breeds);
  }

  @Override
  public void deleteById(long id) throws NotFoundException, ConflictException {
    LOG.trace("delete({})", id);
    LOG.debug("checking if horse is currently in a tournament");
    if (horseMappedToTournamentDao.countTournamentsForHorse(id) > 0) {
      throw new ConflictException("Horse can't be deleted, since it participates in a tournament", Collections.singletonList("Horse is in a tournament"));
    }
    LOG.debug("No conflict - horse with id {} will now be deleted", id);
    horseDao.deleteById(id);
  }
}
