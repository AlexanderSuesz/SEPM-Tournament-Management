package at.ac.tuwien.sepr.assignment.individual.dto;

/**
 * DTO class of a breed used to searches in the create/edit view.
 *
 * @param id   identifier of the breed
 * @param name name of the breed
 */

public record BreedDto(
    long id,
    String name
) {
}
