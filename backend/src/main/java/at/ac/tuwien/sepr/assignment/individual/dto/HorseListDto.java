package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.time.LocalDate;

/**
 * DTO class for list of horses in search view.
 *
 * @param id          identifier of the horse
 * @param name        name of the horse
 * @param sex         gender of the horse
 * @param dateOfBirth birthdate of the horse
 * @param breed       breed of the horse
 */

public record HorseListDto(
    Long id,
    String name,
    Sex sex,
    LocalDate dateOfBirth,
    BreedDto breed
) {
}
