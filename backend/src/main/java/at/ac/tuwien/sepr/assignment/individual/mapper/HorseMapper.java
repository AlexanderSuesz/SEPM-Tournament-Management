package at.ac.tuwien.sepr.assignment.individual.mapper;

import at.ac.tuwien.sepr.assignment.individual.dto.BreedDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HorseMapper {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Convert a horse entity object to a {@link HorseListDto}.
   * The given map of breeds needs to contain the breed of {@code horse}.
   *
   * @param horse the horse to convert
   * @param breeds a map of breeds identified by their id, required for mapping horses
   * @return the converted {@link HorseListDto}
   */
  public HorseListDto entityToListDto(Horse horse, Map<Long, BreedDto> breeds) {
    LOG.trace("entityToListDto({})", horse);
    if (horse == null) {
      return null;
    }

    return new HorseListDto(
        horse.getId(),
        horse.getName(),
        horse.getSex(),
        horse.getDateOfBirth(),
        breedFromMap(horse, breeds)
    );
  }

  /**
   * Convert a horse entity object to a {@link HorseListDto}.
   * The given map of breeds needs to contain the breed of {@code horse}.
   *
   * @param horse the horse to convert
   * @return the converted {@link HorseListDto}
   */
  public HorseDetailDto entityToDetailDto(Horse horse, Map<Long, BreedDto> breeds) {
    LOG.trace("entityToDto({})", horse);
    if (horse == null) {
      return null;
    }

    return new HorseDetailDto(
        horse.getId(),
        horse.getName(),
        horse.getSex(),
        horse.getDateOfBirth(),
        horse.getHeight(),
        horse.getWeight(),
        breedFromMap(horse, breeds)
    );
  }

  private BreedDto breedFromMap(Horse horse, Map<Long, BreedDto> map) {
    var breedId = horse.getBreedId();
    if (breedId == null) {
      return null;
    } else {
      return Optional.ofNullable(map.get(breedId))
          .orElseThrow(() -> new FatalException(
              "Saved horse with id " + horse.getId() + " refers to non-existing breed with id " + breedId));
    }
  }
}
