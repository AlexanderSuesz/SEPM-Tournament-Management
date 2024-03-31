package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO to bundle the query parameters used in searching horses.
 * Each field can be null, in which case this field is not filtered by.
 *
 * @param name         a string which is part of the name of the horse
 * @param sex          gender of the horse
 * @param bornEarliest earliest birth time of the horse
 * @param bornLatest   latest birth time of the horse
 * @param breed        breed of the horse
 * @param limit        the maximum amount of horses one wants as the result
 */

public record HorseSearchDto(
    String name,
    Sex sex,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate bornEarliest,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate bornLatest,
    String breed,
    Integer limit
) {
}
