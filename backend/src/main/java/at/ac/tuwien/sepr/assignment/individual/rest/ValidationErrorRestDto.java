package at.ac.tuwien.sepr.assignment.individual.rest;

import java.util.List;


/**
 * DTO (Data Transfer Object) representing validation error details in REST responses.
 * This class is typically used to convey validation error information from the backend to the client.
 *
 * @param message a brief summary or description of the validation error
 * @param errors  a list of error messages providing detailed information about the validation failures
 */


public record ValidationErrorRestDto(
    String message,
    List<String> errors
) {
}
