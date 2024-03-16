package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.time.LocalDate;

/**
 * DTO encompasses very detailed information about a horse used in details view.
 *
 * @param id          identifier of the horse
 * @param name        name of the horse
 * @param sex         gender of the horse
 * @param dateOfBirth birthdate of the horse
 * @param height      height of the horse
 * @param weight      weight of the horse
 * @param breed       breed of the horse
 */

public record HorseDetailDto(
    Long id,
    String name,
    Sex sex,
    LocalDate dateOfBirth,
    float height,
    float weight,
    BreedDto breed
) {
  /**
   * Creates a new {@link HorseDetailDto} with the addition of the identifier {@code newId}.
   *
   * @param newId the identifier of the horse
   * @return a new {@link HorseDetailDto}
   */
  public HorseDetailDto withId(long newId) {
    return new HorseDetailDto(
        newId,
        name,
        sex,
        dateOfBirth,
        height,
        weight,
        breed);
  }
}
