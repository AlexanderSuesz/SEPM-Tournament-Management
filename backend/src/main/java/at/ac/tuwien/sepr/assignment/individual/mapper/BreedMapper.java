package at.ac.tuwien.sepr.assignment.individual.mapper;

import at.ac.tuwien.sepr.assignment.individual.dto.BreedDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Breed;
import org.springframework.stereotype.Component;

/**
 * BreedMapper is responsible for converting Breed entities to DTOs (Data Transfer Objects).
 */
@Component
public class BreedMapper {

  /**
   * Converts a Breed entity object to a {@link BreedDto}.
   *
   * @param breed the breed to convert
   * @return the converted {@link BreedDto}
   */
  public BreedDto entityToDto(Breed breed) {
    return new BreedDto(breed.getId(), breed.getName());
  }
}
