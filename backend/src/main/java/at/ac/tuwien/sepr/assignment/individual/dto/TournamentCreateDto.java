package at.ac.tuwien.sepr.assignment.individual.dto;

import java.time.LocalDate;

/**
 * DTO encompasses basic information about a tournament and the horses taking part in it.
 * Used when creating a tournament.
 *
 * @param name the name of the tournament
 * @param startDate the start date of the tournament
 * @param endDate the end date of the tournament
 * @param participants the horses taking part in this tournament
 */
public record TournamentCreateDto(
    String name,
    LocalDate startDate,
    LocalDate endDate,
    HorseSelectionDto[] participants
) {
}
