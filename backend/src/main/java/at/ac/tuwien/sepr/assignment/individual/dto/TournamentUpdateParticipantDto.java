package at.ac.tuwien.sepr.assignment.individual.dto;

/**
 * DTO which has necessary information to update a horse tournament mapping.
 *
 * @param horseId the id of the horse
 * @param entryNumber the entry number of the horse
 * @param roundReached the round reached of the horse
 */
public record TournamentUpdateParticipantDto(
    long horseId,
    Long entryNumber,
    Long roundReached
) {
}
