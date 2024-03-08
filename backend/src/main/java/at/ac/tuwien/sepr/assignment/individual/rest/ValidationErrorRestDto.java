package at.ac.tuwien.sepr.assignment.individual.rest;

import java.util.List;

public record ValidationErrorRestDto(
    String message,
    List<String> errors
) {
}
