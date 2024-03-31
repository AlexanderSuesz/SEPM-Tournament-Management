package at.ac.tuwien.sepr.assignment.individual.dto;

import java.time.LocalDate;

/**
 * Dto class for list of tournaments in search view.
 *
 * @param id identifier of the tournament
 * @param name name of the tournament
 * @param startDate start date of the tournament
 * @param endDate end date of the tournament
 */
public record TournamentListDto(
    Long id,
    String name,
    LocalDate startDate,
    LocalDate endDate
) {
}
