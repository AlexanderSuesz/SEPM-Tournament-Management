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
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HorseServiceImpl implements HorseService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final HorseDao dao;
  private final HorseMapper mapper;
  private final HorseValidator validator;
  private final BreedService breedService;

  public HorseServiceImpl(HorseDao dao, HorseMapper mapper, HorseValidator validator, BreedService breedService) {
    this.dao = dao;
    this.mapper = mapper;
    this.validator = validator;
    this.breedService = breedService;
  }

  @Override
  public Stream<HorseListDto> search(HorseSearchDto searchParameters) {
    LOG.info("search({})", searchParameters);
    var horses = dao.search(searchParameters);
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
    var updatedHorse = dao.update(horse);
    var breeds = breedMapForSingleHorse(updatedHorse);
    return mapper.entityToDetailDto(updatedHorse, breeds);
  }


  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {
    LOG.trace("details({})", id);
    Horse horse = dao.getById(id);
    var breeds = breedMapForSingleHorse(horse);
    return mapper.entityToDetailDto(horse, breeds);
  }

  private Map<Long, BreedDto> breedMapForSingleHorse(Horse horse) {
    return breedMapForHorses(Collections.singleton(horse.getBreedId()));
  }



  private Map<Long, BreedDto> breedMapForHorses(Set<Long> horse) {
    return breedService.findBreedsByIds(horse)
        .collect(Collectors.toUnmodifiableMap(BreedDto::id, Function.identity()));
  }

  @Override
  public HorseDetailDto add(HorseDetailDto horse) throws ValidationException {
    LOG.trace("add({})", horse);
    LOG.info("add({})", horse);
    var horses = dao.search(new HorseSearchDto(null, null, null, null, null, null));
    // Select the larges id…
    var ids = horses.stream()
            .map(Horse::getId);
    LOG.info("All found ids are: {}", ids);
    var largestId = ids
            .filter(Objects::nonNull)
            .reduce(Math::min); // ids are starting from negative numbers: -1, -2, -3, ...
    LOG.info("The largest id is: {}", largestId);
    long newId = largestId.isEmpty() ? -1 : largestId.get() - 1;
    HorseDetailDto horseWithID = new HorseDetailDto(newId, horse.name(), horse.sex(), horse.dateOfBirth(), horse.height(), horse.weight(), horse.breed());
    // … then get the breeds all at once.
    validator.validateForUpdate(horseWithID);
    LOG.info("now adding to db: {}", horseWithID);
    var newlyAddedHorse = dao.add(horseWithID);
    var breeds = breedMapForSingleHorse(newlyAddedHorse);
    return mapper.entityToDetailDto(newlyAddedHorse, breeds);
  }
}
