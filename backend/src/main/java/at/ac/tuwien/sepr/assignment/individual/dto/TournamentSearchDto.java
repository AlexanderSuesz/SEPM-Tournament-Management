package at.ac.tuwien.sepr.assignment.individual.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO to bundle the query parameters used in searching tournaments.
 * Each field can be null, in which case this field is not filtered by.
 *
 * @param name a string which is part of the name of the tournament
 * @param earliestTournamentDay the earliest time in which one day of a tournament can take place
 * @param latestTournamentDay the latest time in which one day of a tournament can take place
 * @param limit the maximum amount of tournaments one wants as the result
 */
public record TournamentSearchDto(
    String name,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate earliestTournamentDay,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate latestTournamentDay,
    Integer limit
) {
}