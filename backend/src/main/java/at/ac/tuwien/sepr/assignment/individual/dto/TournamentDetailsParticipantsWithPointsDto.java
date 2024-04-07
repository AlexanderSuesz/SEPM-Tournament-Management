package at.ac.tuwien.sepr.assignment.individual.dto;

import java.time.LocalDate;

/**
 * DTO class representing detailed information about a horse including the points it collected over a timespan.
 */
public class TournamentDetailsParticipantsWithPointsDto {
  private long horseId; // the identifier of the horse
  private String name; // the name of the horse
  private LocalDate dateOfBirth; // the birthdate of the horse
  private Long entryNumber = null; // the start position in the tournament of the horse
  private Long roundReached = null; // the current round in the tournament of the horse
  private long points; // the points acquired by the horse over a given timespan

  public TournamentDetailsParticipantsWithPointsDto(long horseId, String name, LocalDate dateOfBirth, long points) {
    this.horseId = horseId;
    this.name = name;
    this.dateOfBirth = dateOfBirth;
    this.points = points;
  }

  public long getHorseId() {
    return horseId;
  }

  public void setHorseId(long horseId) {
    this.horseId = horseId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public Long getEntryNumber() {
    return entryNumber;
  }

  public void setEntryNumber(Long entryNumber) {
    this.entryNumber = entryNumber;
  }

  public Long getRoundReached() {
    return roundReached;
  }

  public void setRoundReached(Long roundReached) {
    this.roundReached = roundReached;
  }

  public long getPoints() {
    return points;
  }

  public void setPoints(long points) {
    this.points = points;
  }

  @Override
  public String toString() {
    return "TournamentDetailsParticipantsWithPointsDto{"
        + "horseId=" + horseId
        + ", name='" + name + '\''
        + ", dateOfBirth=" + dateOfBirth
        + ", entryNumber=" + entryNumber
        + ", roundReached=" + roundReached
        + ", points=" + points
        + '}';
  }

  public String printJustNameAndScore() {
    return '{'
        + name
        + ", " + points
        + '}';
  }
}
