package at.ac.tuwien.sepr.assignment.individual.dto;

import java.time.LocalDate;

/**
 * A DTO which contains only the most basic data about a horse. Is e.g. used when creating a tournament with horses as its participants.
 *
 * @param id the id of the horse
 * @param name the name of the horse
 * @param dateOfBirth the birthdate of the horse
 */
public record HorseSelectionDto(
    long id,
    String name,
    LocalDate dateOfBirth
) {
}
