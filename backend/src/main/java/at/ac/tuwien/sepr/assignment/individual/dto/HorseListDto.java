package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import java.time.LocalDate;

/**
 * DTO class for list of horses in search view.
 */
public record HorseListDto(
    Long id,
    String name,
    Sex sex,
    LocalDate dateOfBirth,
    BreedDto breed
) {
}
