package at.ac.tuwien.sepr.assignment.individual.dto;

/**
 * DTO of horses in a tournament which has necessary information to update the horse tournament mapping of a tournament.
 *
 * @param id the id of the tournament
 * @param participants the participants of this tournament which are currently in a round
 */
public record TournamentUpdateDto(
    long id,
    TournamentUpdateParticipantDto[] participants
) {
}
