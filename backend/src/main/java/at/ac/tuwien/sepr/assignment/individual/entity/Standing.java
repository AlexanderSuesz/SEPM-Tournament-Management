package at.ac.tuwien.sepr.assignment.individual.entity;

/**
 * Represents a standing, the progress and position of a horse in a specific tournament, in the persistent data store.
 */
public class Standing {
  private long tournamentId;
  private long horseId;
  private Long entryNumber; // will be null if horse has not yet an entry number
  private Long roundReached; // will be null if horse is not yet in a round

  public long getTournamentId() {
    return tournamentId;
  }

  public Standing setTournamentId(long tournamentId) {
    this.tournamentId = tournamentId;
    return this;
  }

  public long getHorseId() {
    return horseId;
  }

  public Standing setHorseId(long horseId) {
    this.horseId = horseId;
    return this;
  }

  public Long getEntryNumber() {
    return entryNumber;
  }

  public Standing setEntryNumber(Long entryNumber) {
    this.entryNumber = entryNumber;
    return this;
  }

  public Long getRoundReached() {
    return roundReached;
  }

  public Standing setRoundReached(Long roundReached) {
    this.roundReached = roundReached;
    return this;
  }

  @Override
  public String toString() {
    return "Standing{"
        + "tournamentId=" + tournamentId
        + ", horseId=" + horseId
        + ", entryNumber=" + entryNumber
        + ", roundReached=" + roundReached
        + '}';
  }
}