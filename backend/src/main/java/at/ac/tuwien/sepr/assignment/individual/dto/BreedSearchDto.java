package at.ac.tuwien.sepr.assignment.individual.dto;

/**
 * DTO to bundle the query parameters used in searching breeds.
 * Each field can be null, in which case this field is not filtered by.
 *
 * @param name  the parameters used for the search
 * @param limit the maximum amount of breeds one wants as the result
 */

public record BreedSearchDto(
    String name,
    Integer limit
) {
}
