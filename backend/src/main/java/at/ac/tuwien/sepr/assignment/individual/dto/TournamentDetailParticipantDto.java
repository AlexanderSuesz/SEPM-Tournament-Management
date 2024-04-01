package at.ac.tuwien.sepr.assignment.individual.dto;

import java.time.LocalDate;

/**
 * DTO which has detailed information about a horse taking part in a tournament.
 *
 * @param horseId the id of the horse
 * @param name the name of the horse
 * @param dateOfBirth the date of birth of the horse
 * @param entryNumber the initial position of the horse in the tournament
 * @param roundReached the round which the horse reached in the tournament
 */
public record TournamentDetailParticipantDto(
    long horseId,
    String name,
    LocalDate dateOfBirth,
    Long entryNumber,
    Long roundReached
) {
}
