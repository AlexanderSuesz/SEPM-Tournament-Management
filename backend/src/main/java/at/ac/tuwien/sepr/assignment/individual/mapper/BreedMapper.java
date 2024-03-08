package at.ac.tuwien.sepr.assignment.individual.mapper;

import at.ac.tuwien.sepr.assignment.individual.dto.BreedDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Breed;
import org.springframework.stereotype.Component;

@Component
public class BreedMapper {
  public BreedDto entityToDto(Breed breed) {
    return new BreedDto(breed.getId(), breed.getName());
  }
}
