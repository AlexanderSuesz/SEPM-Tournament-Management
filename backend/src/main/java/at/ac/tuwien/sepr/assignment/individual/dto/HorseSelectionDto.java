package at.ac.tuwien.sepr.assignment.individual.dto;

import java.time.LocalDate;

public record HorseSelectionDto(
    long id,
    String name,
    LocalDate dateOfBirth
) {
}
