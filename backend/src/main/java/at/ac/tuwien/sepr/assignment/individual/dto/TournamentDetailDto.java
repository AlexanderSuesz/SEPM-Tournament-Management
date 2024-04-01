package at.ac.tuwien.sepr.assignment.individual.dto;

import java.time.LocalDate;

/**
 * DTO encompasses detailed information about a tournament and the horses taking part in it, including their standing in the tournament.
 *
 * @param name the name of the tournament
 * @param startDate the start date of the tournament
 * @param endDate the end date of the tournament
 * @param participants the horses taking part in this tournament and their standing in it
 */
public record TournamentDetailDto(
    long id,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    TournamentDetailParticipantDto[] participants
) {
}
